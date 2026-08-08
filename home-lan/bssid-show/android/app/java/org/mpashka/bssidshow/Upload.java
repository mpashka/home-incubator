package org.mpashka.bssidshow;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.security.KeyChain;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.function.Consumer;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;

import java.io.File;

/**
 * @tag:survey-upload Отправка файла замеров обычным PUT: по HTTP на временный приёмник
 * в домашней сети либо по HTTPS с клиентским сертификатом на app-server.
 *
 * Авторизация — тот же клиентский сертификат, что и у «Моего круга»: он ставится в системное
 * хранилище Android один раз и дальше доступен и браузеру, и этому приложению. Своей копии
 * сертификата и своего пароля приложение не хранит — ключ не покидает хранилище системы,
 * подпись делается там же.
 *
 * Сервер — с сертификатом Let's Encrypt, поэтому проверка его сертификата остаётся штатной:
 * свой TrustManager не подставляем.
 */
final class Upload {

    private Upload() {}

    /** Результат: null — успех, иначе текст ошибки для показа человеку. */
    interface Done {
        void finished(String error);
    }

    static void put(Context ctx, String alias, File file, String base, Done done) {
        Handler main = new Handler(Looper.getMainLooper());
        // Сеть с главного потока Android запрещает, а тащить ради одного запроса исполнителя
        // задач незачем.
        new Thread(() -> {
            String error = null;
            try {
                send(ctx, alias, file, base);
            } catch (Exception e) {
                error = e.getMessage() == null ? e.toString() : e.getMessage();
                Log.w(Wifi.TAG, "загрузка не удалась", e);
            }
            String result = error;
            main.post(() -> done.finished(result));
        }).start();
    }

    static void put(Context ctx, File file, String base, Done done) {
        put(ctx, null, file, base, done);
    }

    static void certificateAvailable(Context ctx, String alias, Consumer<Boolean> done) {
        Handler main = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            boolean available;
            try {
                available = KeyChain.getPrivateKey(ctx, alias) != null
                        && KeyChain.getCertificateChain(ctx, alias) != null;
            } catch (Exception e) {
                available = false;
            }
            boolean result = available;
            main.post(() -> done.accept(result));
        }).start();
    }

    private static void send(Context ctx, String alias, File file, String base) throws Exception {
        URL url = new URL(base.endsWith("/") ? base + file.getName() : base + "/" + file.getName());
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        if (alias != null) {
            PrivateKey key = KeyChain.getPrivateKey(ctx, alias);
            X509Certificate[] chain = KeyChain.getCertificateChain(ctx, alias);
            if (key == null || chain == null) {
                throw new IOException("сертификат «" + alias + "» недоступен — переустановите его");
            }
            SSLContext ssl = SSLContext.getInstance("TLS");
            ssl.init(new KeyManager[]{new Single(alias, key, chain)}, null, null);
            ((HttpsURLConnection) c).setSSLSocketFactory(ssl.getSocketFactory());
        }
        c.setRequestMethod("PUT");
        c.setDoOutput(true);
        c.setFixedLengthStreamingMode(file.length());
        c.setRequestProperty("Content-Type", "text/csv; charset=utf-8");
        c.setConnectTimeout(15_000);
        c.setReadTimeout(30_000);
        try (OutputStream out = c.getOutputStream(); FileInputStream in = new FileInputStream(file)) {
            byte[] buf = new byte[8192];
            for (int n = in.read(buf); n > 0; n = in.read(buf)) out.write(buf, 0, n);
        }
        int code = c.getResponseCode();
        String message = c.getResponseMessage();
        c.disconnect();
        if (code / 100 != 2) {
            // 400 — сервер не увидел сертификата, 403 — увидел, но CN не в белом списке.
            throw new IOException("сервер ответил " + code + " " + message);
        }
    }

    /** KeyManager на один заранее выбранный сертификат из системного хранилища. */
    private static final class Single extends X509ExtendedKeyManager {

        private final String alias;
        private final PrivateKey key;
        private final X509Certificate[] chain;

        Single(String alias, PrivateKey key, X509Certificate[] chain) {
            this.alias = alias;
            this.key = key;
            this.chain = chain;
        }

        @Override public String chooseClientAlias(String[] types, Principal[] issuers, Socket s) {
            return alias;
        }

        @Override public String chooseEngineClientAlias(String[] types, Principal[] is, SSLEngine e) {
            return alias;
        }

        @Override public X509Certificate[] getCertificateChain(String a) { return chain; }

        @Override public PrivateKey getPrivateKey(String a) { return key; }

        @Override public String[] getClientAliases(String type, Principal[] issuers) {
            return new String[]{alias};
        }

        @Override public String[] getServerAliases(String type, Principal[] issuers) { return null; }

        @Override public String chooseServerAlias(String type, Principal[] is, Socket s) { return null; }
    }
}
