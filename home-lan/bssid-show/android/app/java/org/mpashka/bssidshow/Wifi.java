package org.mpashka.bssidshow;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * @tag:bssid-read Чтение текущей точки. Работает только с переднего плана и только при
 * выданном ACCESS_FINE_LOCATION и включённых службах геолокации — иначе система молча
 * подставляет 02:00:00:00:00:00. Молчать в ответ нельзя: причину возвращаем текстом.
 */
final class Wifi {

    private Wifi() {}

    static final String TAG = "bssid-show";

    private static final String FAKE = "02:00:00:00:00:00";

    static final class State {
        String bssid;          // null, если прочитать не вышло
        String ssid;
        int rssi = Rooms.RSSI_NONE;
        int freq;              // МГц, чтобы отличать 2.4 от 5 в замерах
        String error;          // null == всё хорошо; иначе готовый текст для пользователя
        boolean needsPermission;
        boolean needsLocationOn;
    }

    static State read(Context ctx) {
        State s = new State();
        WifiManager wm = ctx.getSystemService(WifiManager.class);
        if (wm == null || !wm.isWifiEnabled()) {
            s.error = "Wi-Fi выключен";
            return s;
        }
        WifiInfo info = info(ctx, wm);
        if (info == null || info.getBSSID() == null) {
            s.error = "Нет подключения к Wi-Fi";
            return s;
        }
        s.rssi = info.getRssi();
        s.freq = info.getFrequency();
        s.ssid = info.getSSID() == null ? null : info.getSSID().replace("\"", "");
        if ("<unknown ssid>".equals(s.ssid)) s.ssid = null;

        String bssid = info.getBSSID();
        if (FAKE.equals(bssid)) {
            // Заглушка приходит без ошибки и без исключения — разбираем причину сами.
            if (!hasPermission(ctx)) {
                s.needsPermission = true;
                s.error = "Нет разрешения на геолокацию — система прячет BSSID";
            } else if (!locationEnabled(ctx)) {
                s.needsLocationOn = true;
                s.error = "Службы геолокации выключены — система прячет BSSID";
            } else {
                s.error = "Система скрыла BSSID (приложение должно быть на переднем плане)";
            }
            return s;
        }
        s.bssid = bssid.toLowerCase();
        return s;
    }

    /**
     * Два источника, оба живые: у каждого своя проверка прав, и урезают они по-разному.
     * Берём тот, который отдал настоящий адрес, — на HyperOS 16 современный путь молча
     * возвращает 02:00:00:00:00:00 даже с выданным разрешением и на переднем плане.
     */
    private static WifiInfo info(Context ctx, WifiManager wm) {
        WifiInfo modern = fromCapabilities(ctx);
        if (real(modern)) return modern;
        // ponytail: getConnectionInfo() устарел с API 31, но пока это единственный путь,
        // отдающий BSSID на этом телефоне. Апгрейд — выкинуть, если однажды перестанет.
        WifiInfo legacy = wm.getConnectionInfo();
        Log.i(TAG, "bssid: getTransportInfo=" + (modern == null ? "null" : modern.getBSSID())
                + " getConnectionInfo=" + (legacy == null ? "null" : legacy.getBSSID()));
        return real(legacy) ? legacy : modern != null ? modern : legacy;
    }

    private static WifiInfo fromCapabilities(Context ctx) {
        ConnectivityManager cm = ctx.getSystemService(ConnectivityManager.class);
        NetworkCapabilities nc = cm == null || cm.getActiveNetwork() == null
                ? null : cm.getNetworkCapabilities(cm.getActiveNetwork());
        if (nc != null && nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                && nc.getTransportInfo() instanceof WifiInfo) {
            return (WifiInfo) nc.getTransportInfo();
        }
        return null;
    }

    private static boolean real(WifiInfo i) {
        return i != null && i.getBSSID() != null && !FAKE.equals(i.getBSSID());
    }

    static boolean hasPermission(Context ctx) {
        return ctx.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    static boolean locationEnabled(Context ctx) {
        LocationManager lm = ctx.getSystemService(LocationManager.class);
        return lm != null && lm.isLocationEnabled();
    }

    static void save(Context ctx, State s) {
        RoomWidget.prefs(ctx).edit()
                .putString("bssid", s.bssid)
                .putString("ssid", s.ssid)
                .putInt("rssi", s.rssi)
                .putString("err", s.error)
                .putLong("when", System.currentTimeMillis())
                .apply();
    }
}
