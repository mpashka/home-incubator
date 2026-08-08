package org.mpashka.bssidshow;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Единственный экран: что сейчас видно, зачем приложению геолокация и карта BSSID→комната.
 * Карта — обычный текст: шесть строк можно вставить целиком из docs/home-aps.md, а BSSID
 * текущей точки добавляется кнопкой, чтобы не набирать MAC руками.
 */
public class MainActivity extends Activity {

    private TextView status;
    private EditText map;
    private Button perm, loc, add;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.main);
        status = findViewById(R.id.status);
        map = findViewById(R.id.map);
        perm = findViewById(R.id.perm);
        loc = findViewById(R.id.loc);
        add = findViewById(R.id.add);

        map.setText(RoomWidget.mapping(this));

        perm.setOnClickListener(v ->
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1));
        loc.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
        add.setOnClickListener(v -> addCurrent());
        findViewById(R.id.save).setOnClickListener(v -> save());
        findViewById(R.id.refresh).setOnClickListener(v -> refresh());
        findViewById(R.id.survey).setOnClickListener(v ->
                startActivity(new Intent(this, SurveyActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] p, int[] granted) {
        refresh();
    }

    private void save() {
        RoomWidget.prefs(this).edit().putString("map", map.getText().toString()).apply();
        RoomWidget.updateAll(this);
        Toast.makeText(this, "Карта сохранена", Toast.LENGTH_SHORT).show();
        refresh();
    }

    /** Дописать текущую точку в карту — набирать BSSID руками невыносимо. */
    private void addCurrent() {
        String bssid = RoomWidget.prefs(this).getString("bssid", null);
        if (bssid == null) {
            Toast.makeText(this, "BSSID сейчас не читается", Toast.LENGTH_SHORT).show();
            return;
        }
        String text = map.getText().toString();
        if (!text.isEmpty() && !text.endsWith("\n")) text += "\n";
        map.setText(text + Rooms.normalize(bssid) + " ");
        map.setSelection(map.getText().length());
        Toast.makeText(this, "Допишите название комнаты и сохраните", Toast.LENGTH_LONG).show();
    }

    private void refresh() {
        Wifi.State s = Wifi.read(this);
        Wifi.save(this, s);
        RoomWidget.updateAll(this);

        show(perm, s.needsPermission || !Wifi.hasPermission(this));
        show(loc, s.needsLocationOn || !Wifi.locationEnabled(this));
        show(add, s.bssid != null);

        StringBuilder sb = new StringBuilder();
        if (s.error != null) {
            sb.append("⚠ ").append(s.error);
        } else {
            String room = Rooms.nameFor(map.getText().toString(), s.bssid);
            sb.append("Сеть: ").append(s.ssid == null ? "—" : s.ssid).append('\n');
            sb.append("Точка: ").append(s.bssid).append('\n');
            sb.append("Комната: ").append(room == null ? "не в карте" : room);
            if (room != null && Rooms.icon(room) == 0) {
                // Глиф рисуется как есть, но длинное слово в клетке 1x1 сожмётся в нечитаемое.
                sb.append(Rooms.glyphLength(room) > 2
                        ? " (в виджете «" + Rooms.glyph(room) + "» не поместится, поставь эмодзи)"
                        : " (рисуется глифом «" + Rooms.glyph(room) + "»)");
            }
            sb.append('\n');
            sb.append("Сигнал: ").append(s.rssi).append(" dBm — ")
              .append(Rooms.level(s.rssi)).append(" из 4");
        }
        sb.append("\nПрочитано: ")
          .append(new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date()));
        status.setText(sb.toString());
    }

    private static void show(View v, boolean visible) {
        v.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
