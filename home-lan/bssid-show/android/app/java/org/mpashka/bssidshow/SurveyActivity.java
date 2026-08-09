package org.mpashka.bssidshow;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.security.KeyChain;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @tag:site-survey Обход точек замера: приложение ведёт по маршруту по одной точке, на каждой
 * пишет уровни всех видимых сетей в CSV. В конце обхода файл можно отправить временно
 * запущенному приёмнику на ПК или на app-server ({@link Upload}); на телефоне он остаётся
 * всегда и доступен через `adb pull`.
 *
 * Данные нужны, чтобы подстраивать мощность точек под автоматический роуминг, поэтому пишутся
 * уровни ВСЕХ видимых BSSID сразу, а не только текущей: переключение решает разница между
 * точками, а не абсолютный уровень одной.
 *
 * Одна кнопка. Маршрут задан списком, значит порядок уже определён, и выбирать точку руками
 * незачем: после удачного замера приложение само переходит к следующей и говорит, куда идти.
 */
public class SurveyActivity extends Activity {

    /** Сколько стоять на точке. Меньше — не успевает прийти ни один свежий скан. */
    private static final long WINDOW_MS = 25_000;

    private static final String HEADER = "point,scan,ts,bssid,ssid,freq_mhz,rssi,connected,cached,device\n";
    private static final String DEVICE = device(Build.MANUFACTURER, Build.MODEL);
    private static final SimpleDateFormat TS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private static final SimpleDateFormat FILE_TS = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.US);

    private final Handler handler = new Handler(Looper.getMainLooper());

    private WifiManager wm;
    private List<String> points;
    private boolean fromFile;
    private int index;
    private File csv;

    private TextView title, status, path;
    private View uploadActions;
    private EditText pcUrl;
    private Button measure, sendPc, sendServer, resend;

    private BroadcastReceiver receiver;
    private long deadline, lastScanTs, measureStartedUs;
    private int scans;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.survey);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        wm = getSystemService(WifiManager.class);
        title = findViewById(R.id.point);
        status = findViewById(R.id.status);
        path = findViewById(R.id.path);
        measure = findViewById(R.id.measure);
        uploadActions = findViewById(R.id.upload_actions);
        pcUrl = findViewById(R.id.pc_url);
        sendPc = findViewById(R.id.send_pc);
        sendServer = findViewById(R.id.send_server);
        resend = findViewById(R.id.resend);
        pcUrl.setText(RoomWidget.prefs(this).getString("pc_url", getString(R.string.pc_upload_url)));

        points = readPoints();
        resumeOrStart();
        measure.setOnClickListener(v -> {
            if (index < points.size()) startMeasure();
            else restart();
        });
        sendPc.setOnClickListener(v -> uploadPc());
        sendServer.setOnClickListener(v -> uploadServer());
        resend.setOnClickListener(v -> {
            RoomWidget.prefs(this).edit().remove("survey_sent_to").apply();
            show();
        });
        show();
    }

    private File dir() {
        File d = getExternalFilesDir(null);
        return d != null ? d : getFilesDir();
    }

    private File pointsFile() {
        return new File(dir(), "points.txt");
    }

    /**
     * Маршрут: файл снаружи, если он положен (ansible), иначе — комнаты из карты BSSID.
     * Имён точек в приложении не зашито: незачем спрашивать у пользователя то, что уже
     * записано в карте.
     */
    private List<String> readPoints() {
        List<String> out = new ArrayList<>();
        File f = pointsFile();
        if (f.exists()) {
            try (BufferedReader r = new BufferedReader(new FileReader(f))) {
                for (String line = r.readLine(); line != null; line = r.readLine()) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) out.add(line);
                }
            } catch (IOException e) {
                Log.w(Wifi.TAG, "points.txt: " + e);
            }
            fromFile = !out.isEmpty();
        }
        if (out.isEmpty()) {
            fromFile = false;
            out = Rooms.rooms(RoomWidget.mapping(this));
        }
        return out;
    }

    /** Обход можно прервать и вернуться: место в маршруте и файл переживают выход. */
    private void resumeOrStart() {
        SharedPreferences p = RoomWidget.prefs(this);
        String saved = p.getString("survey_file", null);
        File f = saved == null ? null : new File(dir(), saved);
        int savedIndex = Math.min(p.getInt("survey_index", 0), points.size());
        // `adb install -r` сохраняет prefs. Завершённый старый файл можно отправить, но
        // незавершённый CSV прежнего формата дописывать нельзя: получится смешанный файл.
        if (f != null && f.exists() && (savedIndex >= points.size() || currentFormat(f))) {
            csv = f;
            index = savedIndex;
        } else {
            restart();
        }
    }

    private static boolean currentFormat(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return currentHeader(reader.readLine());
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean currentHeader(String line) {
        return HEADER.trim().equals(line);
    }

    private void restart() {
        csv = new File(dir(), "survey-" + FILE_TS.format(new Date()) + ".csv");
        index = 0;
        RoomWidget.prefs(this).edit().remove("survey_sent_to").apply();
        save();
        show();
    }

    private void save() {
        RoomWidget.prefs(this).edit()
                .putString("survey_file", csv.getName())
                .putInt("survey_index", index)
                .apply();
    }

    private void show() {
        boolean done = !points.isEmpty() && index >= points.size();
        String sentTo = RoomWidget.prefs(this).getString("survey_sent_to", null);
        uploadActions.setVisibility(done && sentTo == null ? View.VISIBLE : View.GONE);
        resend.setVisibility(done && sentTo != null ? View.VISIBLE : View.GONE);
        sendServer.setVisibility(View.GONE);
        String alias = RoomWidget.prefs(this).getString("cert_alias", null);
        if (done && alias != null) {
            Upload.certificateAvailable(this, alias, available -> {
                if (!points.isEmpty() && index >= points.size()) {
                    sendServer.setVisibility(available ? View.VISIBLE : View.GONE);
                }
            });
        }
        path.setText("Маршрут: " + (fromFile ? pointsFile() : "комнаты из карты BSSID")
                + "\nЗамеры: " + csv
                + "\n\nЗабрать с ноутбука:\nadb pull " + csv);
        if (points.isEmpty()) {
            title.setText("Маршрута нет");
            status.setText("Карта BSSID пуста, а файла " + pointsFile() + " нет. "
                    + "Заполните карту на главном экране или положите список точек в файл.");
            measure.setEnabled(false);
        } else if (done) {
            title.setText("Обход закончен");
            status.setText("Пройдено точек: " + points.size() + ". Файл "
                    + (sentTo == null ? "готов и остался на телефоне."
                    : "успешно отправлен: " + sentTo + "."));
            measure.setText("Начать обход заново");
            measure.setEnabled(true);
        } else {
            title.setText(where(index));
            status.setText("Точка " + (index + 1) + " из " + points.size()
                    + ". Нажмите «Замер» и стойте на месте.");
            measure.setText("Замер");
            measure.setEnabled(true);
        }
    }

    private String where(int i) {
        return fromFile
                ? "Встаньте на точку «" + points.get(i) + "»"
                : "Встаньте в центре комнаты «" + points.get(i) + "»";
    }

    private void startMeasure() {
        measure.setEnabled(false);
        scans = 0;
        lastScanTs = 0;
        measureStartedUs = SystemClock.elapsedRealtimeNanos() / 1_000;

        receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context c, Intent i) { collect(false); }
        };
        registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION),
                Context.RECEIVER_NOT_EXPORTED);
        // ponytail: startScan() устарел и с Android 10 троттлится (4 вызова в 2 минуты) —
        // поэтому один вызов на точку, а дальше слушаем сканы, которые система делает сама.
        // Вызов может отказать (троттлинг) или бросить SecurityException, если прошивка
        // считает иначе. Ронять из-за этого весь обход нельзя: без своего скана замер всё
        // равно соберётся из системных, просто медленнее.
        boolean asked;
        try {
            asked = wm.startScan();
        } catch (SecurityException e) {
            Log.w(Wifi.TAG, "startScan отказал: " + e);
            asked = false;
        }
        if (!asked) status.setText("Свой скан не запустился — ждём системный, это дольше.");

        deadline = SystemClock.elapsedRealtime() + WINDOW_MS;
        handler.post(tick);
    }

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            long left = deadline - SystemClock.elapsedRealtime();
            if (left > 0) {
                status.setText("Стойте на месте: " + (left / 1000 + 1) + " с. Сканов: " + scans);
                handler.postDelayed(this, 500);
            } else {
                finishMeasure();
            }
        }
    };

    private void finishMeasure() {
        unregister();
        if (scans == 0) {
            // Ни одного свежего скана: пишем кэш системы, помечаем — и НЕ идём дальше.
            // Зачесть такую точку как снятую значило бы выдать старые данные за замер.
            collect(true);
            status.setText("Свежих сканов не пришло — записан кэш системы (cached=1).\n"
                    + "Постойте ещё и нажмите «Замер» снова.");
            measure.setEnabled(true);
            return;
        }
        int done = scans;
        index++;
        save();
        show();
        if (index < points.size()) {
            status.setText("Готово, сканов: " + done + ".\nСледующая: "
                    + where(index).toLowerCase(new Locale("ru")) + ".");
        }
    }

    /** @tag:survey-upload Прямой PUT на одноразовый приёмник в домашней сети. */
    private void uploadPc() {
        String url = pcUrl.getText().toString().trim();
        if (!url.startsWith("http://")) {
            status.setText("Адрес ПК должен начинаться с http://");
            return;
        }
        RoomWidget.prefs(this).edit().putString("pc_url", url).apply();
        sending(true);
        status.setText("Отправляю " + csv.getName() + " на ПК…");
        Upload.put(this, csv, url, error -> uploadFinished("ПК", error));
    }

    /**
     * Отправка результата. Сертификат берём из системного хранилища — тот же, что у
     * «Моего круга»; при первой отправке система спросит, какой именно.
     */
    private void uploadServer() {
        SharedPreferences p = RoomWidget.prefs(this);
        String alias = p.getString("cert_alias", null);
        if (alias == null) {
            status.setText("Выберите сертификат для отправки на сервер.");
            KeyChain.choosePrivateKeyAlias(this, chosen -> runOnUiThread(() -> {
                if (chosen == null) {
                    status.setText("Сертификат не выбран — файл остался на телефоне.\n"
                            + "Его всегда можно забрать командой ниже.");
                    return;
                }
                p.edit().putString("cert_alias", chosen).apply();
                uploadServer();
            }), null, null, null, -1, null);
            return;
        }
        sending(true);
        status.setText("Отправляю " + csv.getName() + " на app-server…");
        Upload.put(this, alias, csv, getString(R.string.upload_url), error -> {
            uploadFinished("app-server", error);
        });
    }

    private void uploadFinished(String destination, String error) {
        sending(false);
        if (error == null) {
            RoomWidget.prefs(this).edit().putString("survey_sent_to", destination).apply();
            show();
        } else {
            // Файл на телефоне остаётся всегда: неудачная отправка не должна выглядеть
            // как отправленная, и терять из-за неё замеры тоже нельзя.
            status.setText("Отправить не вышло: " + error
                    + "\nФайл остался на телефоне, можно повторить или забрать через adb.");
        }
    }

    private void sending(boolean value) {
        measure.setEnabled(!value);
        sendPc.setEnabled(!value);
        sendServer.setEnabled(!value);
        pcUrl.setEnabled(!value);
    }

    private void collect(boolean cached) {
        List<ScanResult> results = wm.getScanResults();
        if (results == null || results.isEmpty()) return;
        if (!cached) {
            List<ScanResult> fresh = new ArrayList<>();
            for (ScanResult r : results) {
                if (fresh(r.timestamp, measureStartedUs)) fresh.add(r);
            }
            if (fresh.isEmpty()) return;
            results = fresh;
        }
        long ts = 0;
        for (ScanResult r : results) ts = Math.max(ts, r.timestamp);
        if (!cached && ts == lastScanTs) return;   // тот же самый скан, второй раз не пишем
        lastScanTs = ts;
        if (!cached) scans++;
        // Читаем текущую точку заново на каждый скан: нужен уровень в этот момент, а не
        // в момент нажатия кнопки.
        write(results, Wifi.read(this), cached);
    }

    private void write(List<ScanResult> results, Wifi.State me, boolean cached) {
        String point = points.get(index);
        String now = TS.format(new Date());
        StringBuilder sb = new StringBuilder();
        boolean seen = false;
        for (ScanResult r : results) {
            boolean isMe = r.BSSID != null && r.BSSID.equalsIgnoreCase(me.bssid);
            seen |= isMe;
            row(sb, point, now, r.BSSID, r.SSID, r.frequency, r.level, isMe, cached);
        }
        // Сканер обходит каналы, и точка, на которой телефон сидит, в конкретный проход
        // попадает не всегда. Без этой строки «кого он выбрал здесь» — главное число файла —
        // просто потерялось бы, причём молча.
        if (!seen && me.bssid != null) {
            row(sb, point, now, me.bssid, me.ssid, me.freq, me.rssi, true, cached);
        }
        boolean fresh = !csv.exists();
        try (FileWriter w = new FileWriter(csv, true)) {
            if (fresh) w.write(HEADER);
            w.write(sb.toString());
        } catch (IOException e) {
            Log.w(Wifi.TAG, "не смог записать замер: " + e);
            status.setText("Не смог записать файл: " + e.getMessage());
        }
    }

    private void row(StringBuilder sb, String point, String now, String bssid, String ssid,
                     int freq, int rssi, boolean connected, boolean cached) {
        sb.append(csv(point)).append(',')
          .append(scans).append(',')
          .append(now).append(',')
          .append(bssid).append(',')
          .append(csv(ssid == null ? "" : ssid.replace("\"", ""))).append(',')
          .append(freq).append(',')
          .append(rssi).append(',')
          .append(connected ? 1 : 0).append(',')
          .append(cached ? 1 : 0).append(',')
          .append(csv(DEVICE)).append('\n');
    }

    private static String csv(String s) {
        return s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")
                ? '"' + s.replace("\"", "\"\"") + '"' : s;
    }

    private static String device(String manufacturer, String model) {
        return (manufacturer + " " + model).trim();
    }

    private static boolean fresh(long scanTimestampUs, long measureStartedUs) {
        return scanTimestampUs >= measureStartedUs;
    }

    /** Самопроверка CSV и фильтра свежести без телефона. См. build.sh check. */
    public static void main(String[] args) {
        assertEq("Lenovo TB376FC", csv(device("Lenovo", "TB376FC")));
        assertEq("\"Acme, Inc. Model \"\"A\"\"\"", csv(device("Acme, Inc.", "Model \"A\"")));
        if (currentHeader("point,scan,ts,bssid,ssid,freq_mhz,rssi,connected,cached")) {
            throw new AssertionError("старый CSV принят за текущий");
        }
        if (!currentHeader(HEADER.trim())) throw new AssertionError("текущий CSV не распознан");
        if (fresh(999, 1_000)) throw new AssertionError("старый ScanResult принят за свежий");
        if (!fresh(1_000, 1_000)) throw new AssertionError("новый ScanResult отброшен");
        System.out.println("SurveyActivity: ok");
    }

    private static void assertEq(String want, String got) {
        if (!want.equals(got)) throw new AssertionError("ожидалось " + want + ", получено " + got);
    }

    private void unregister() {
        handler.removeCallbacks(tick);
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Экран ушёл — замер прерван. Дописывать его потом нечестно: точка уже другая.
        if (receiver != null) {
            unregister();
            status.setText("Замер прерван. Нажмите «Замер» ещё раз.");
            measure.setEnabled(true);
        }
    }
}
