package com.itzblaze;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class HttpUtility {
    public static final int METHOD_GET = 0;

    public static final int METHOD_POST = 1;

    public static void newRequest(final String web_url, final int method, final HashMap<String, String> params, final Callback callback) {
        (new Thread(new Runnable() {
            public void run() {
                try {
                    String url = web_url;
                    if (method == 0 && params != null)
                        for (Map.Entry<String, String> item : (Iterable<Map.Entry<String, String>>)params.entrySet()) {
                            String key = URLEncoder.encode(item.getKey(), "UTF-8");
                            String value = URLEncoder.encode(item.getValue(), "UTF-8");
                            if (!url.contains("?")) {
                                url = url + "?" + key + "=" + value;
                                continue;
                            }
                            url = url + "&" + key + "=" + value;
                        }
                    HttpURLConnection urlConnection = (HttpURLConnection)(new URL(url)).openConnection();
                    urlConnection.setUseCaches(false);
                    urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    urlConnection.setRequestProperty("charset", "utf-8");
                    if (method == 0) {
                        urlConnection.setRequestMethod("GET");
                    } else if (method == 1) {
                        urlConnection.setDoOutput(true);
                        urlConnection.setRequestMethod("POST");
                    }
                    if (method == 1 && params != null) {
                        StringBuilder postData = new StringBuilder();
                        for (Map.Entry<String, String> item : (Iterable<Map.Entry<String, String>>)params.entrySet()) {
                            if (postData.length() != 0)
                                postData.append('&');
                            postData.append(URLEncoder.encode(item.getKey(), "UTF-8"));
                            postData.append('=');
                            postData.append(URLEncoder.encode(String.valueOf(item.getValue()), "UTF-8"));
                        }
                        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
                        urlConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                        urlConnection.getOutputStream().write(postDataBytes);
                    }
                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == 200 && callback != null) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null)
                            response.append(line);
                        callback.OnSuccess(response.toString());
                        reader.close();
                    } else if (callback != null) {
                        callback.OnError(responseCode, urlConnection.getResponseMessage());
                    }
                    urlConnection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                    if (callback != null)
                        callback.OnError(500, e.getLocalizedMessage());
                }
            }
        })).start();
    }

    public static interface Callback {
        void OnSuccess(String param1String);

        void OnError(int param1Int, String param1String);
    }
}
