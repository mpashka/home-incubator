package org.mpashka.bssidshow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Нажали на виджет. Прозрачная активность нужна ровно затем, чтобы процесс на мгновение
 * оказался на переднем плане: в фоне система BSSID не отдаёт.
 *
 * ponytail: обновление по нажатию — потолок в том, что между нажатиями надпись стареет
 * (виджет её гасит). Апгрейд — foreground service с NetworkCallback, если понадобится
 * видеть переходы, не трогая телефон. Цена — постоянное уведомление в шторке.
 */
public class RefreshActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();
        Wifi.State s = Wifi.read(this);
        Wifi.save(this, s);
        RoomWidget.updateAll(this);
        // Причину, которую пользователь может устранить, показываем, а не проглатываем.
        if (s.needsPermission || s.needsLocationOn) {
            startActivity(new Intent(this, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        overridePendingTransition(0, 0);
    }
}
