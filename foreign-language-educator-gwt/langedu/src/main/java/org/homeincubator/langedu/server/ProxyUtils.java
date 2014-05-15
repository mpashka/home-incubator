package org.homeincubator.langedu.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

/**
 */
public class ProxyUtils {

    private static final Logger log = Logger.getLogger(ProxyUtils.class.getName());

    public static void proxyRequest(String urlString, String params, OutputStream out) {
        try {
            proxyRequest0(urlString, params, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static InputStream proxyRequest(String urlString, String params) {
        try {
            InputStream inputStream = proxyRequest0(urlString, params);
            return inputStream;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static InputStream proxyRequest0(String urlString, String params) throws Exception {
        log.finest("Proxy request to " + urlString + " with params " + params);

        URL url1 = new URL(urlString);
        URLConnection urlConnection = url1.openConnection();
        HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
        httpConnection.setRequestMethod("POST");
        String charset = "utf-8";

//        urlConnection.setRequestProperty("User-Agent", USER_AGENT);
        urlConnection.setRequestProperty("Accept-Charset", charset);
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
        byte[] paramsBytes = params.getBytes();
        urlConnection.setRequestProperty("Content-Length", Integer.toString(paramsBytes.length));
        urlConnection.setUseCaches(false);

        urlConnection.setDoOutput(true);
        OutputStream outputStream = urlConnection.getOutputStream();
        outputStream.write(paramsBytes);
        outputStream.close();


        int responseCode = httpConnection.getResponseCode();
        if (responseCode != 200) {
//            debug("Error code: "+ responseCode);
            return null;
        }
        InputStream inputStream = urlConnection.getInputStream();

//        return inputStream;

        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        StringWriter out = new StringWriter();
        String inLine;
        while ((inLine = in.readLine()) != null) {
            log.severe("> " + inLine);
            out.write(inLine);
            out.write('\n');
        }
        out.close();
        return new ByteArrayInputStream(out.toString().getBytes());
    }

    public static void proxyRequest0(String urlString, String params, OutputStream out) throws Exception {
        InputStream inputStream = proxyRequest0(urlString, params);

        byte[] buf = new byte[1000];
        int readLen;
        while ((readLen = inputStream.read(buf)) != -1) {
            out.write(buf, 0, readLen);
        }

//        httpConnection.disconnect();
    }

}
