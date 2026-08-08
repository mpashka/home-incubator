package org.mpashka.bssidshow;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Виджет 1x1: иконка комнаты + четыре палочки уровня сигнала.
 *
 * Рисует только по сохранённому состоянию и сам Wi-Fi никогда не читает: onUpdate приходит
 * в фоне, а в фоне система отдаёт заглушку вместо BSSID. Читают {@link RefreshActivity}
 * и {@link MainActivity} — то есть передний план.
 */
public class RoomWidget extends AppWidgetProvider {

    /** Показания старше — гасим иконку: то, что видно, снято давно и могло устареть. */
    private static final long STALE_MS = 10 * 60 * 1000L;

    private static final int[] BARS = {R.id.b1, R.id.b2, R.id.b3, R.id.b4};
    private static final int BAR_OFF = 0x33FFFFFF;

    @Override
    public void onUpdate(Context ctx, AppWidgetManager m, int[] ids) {
        m.updateAppWidget(ids, build(ctx));
    }

    static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences("bssid-show", Context.MODE_PRIVATE);
    }

    /** Карта BSSID→комната. Данные, а не код: правится в приложении без пересборки. */
    static String mapping(Context ctx) {
        return prefs(ctx).getString("map", ctx.getString(R.string.default_mapping));
    }

    static void updateAll(Context ctx) {
        AppWidgetManager m = AppWidgetManager.getInstance(ctx);
        m.updateAppWidget(m.getAppWidgetIds(new ComponentName(ctx, RoomWidget.class)), build(ctx));
    }

    static RemoteViews build(Context ctx) {
        SharedPreferences p = prefs(ctx);
        String err = p.getString("err", null);
        String bssid = p.getString("bssid", null);
        String room = err != null ? null : Rooms.nameFor(mapping(ctx), bssid);

        RemoteViews v = new RemoteViews(ctx.getPackageName(), R.layout.widget);
        boolean stale = System.currentTimeMillis() - p.getLong("when", 0) > STALE_MS;

        int icon = Rooms.icon(room);
        if (icon == 0 && room != null) {
            // Комната есть, встроенной иконки нет — рисуем глиф из карты (эмодзи).
            v.setViewVisibility(R.id.icon, View.GONE);
            v.setViewVisibility(R.id.glyph, View.VISIBLE);
            v.setTextViewText(R.id.glyph, Rooms.glyph(room));
        } else {
            if (icon == 0) {
                // Отказ и «точка не в карте» — разные вещи, и выглядеть должны по-разному.
                icon = err != null
                        ? android.R.drawable.ic_dialog_alert : android.R.drawable.ic_menu_help;
            }
            v.setViewVisibility(R.id.glyph, View.GONE);
            v.setViewVisibility(R.id.icon, View.VISIBLE);
            v.setImageViewResource(R.id.icon, icon);
            v.setInt(R.id.icon, "setImageAlpha", stale ? 90 : 255);
        }

        // Устарели показания — устарел и уровень. Гасим палочки, а не показываем вчерашний
        // сигнал как сегодняшний: цветные эмодзи притушить нечем, и это единственный
        // признак устаревания, который виден в обоих режимах.
        int lit = err != null || stale ? 0 : Rooms.level(p.getInt("rssi", Rooms.RSSI_NONE));
        int color = lit >= 3 ? 0xFF4CAF50 : lit == 2 ? 0xFFFFC107 : 0xFFF44336;
        for (int i = 0; i < BARS.length; i++) {
            v.setInt(BARS[i], "setBackgroundColor", i < lit ? color : BAR_OFF);
        }

        v.setContentDescription(R.id.root, describe(err, room, bssid, lit, stale));

        Intent i = new Intent(ctx, RefreshActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        v.setOnClickPendingIntent(R.id.root, PendingIntent.getActivity(ctx, 0, i,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
        return v;
    }

    private static String describe(String err, String room, String bssid, int lit, boolean stale) {
        if (err != null) return err;
        String where = room != null ? room : "точка " + bssid + " не в карте";
        return where + ", сигнал " + lit + " из 4" + (stale ? ", показания устарели" : "");
    }
}
