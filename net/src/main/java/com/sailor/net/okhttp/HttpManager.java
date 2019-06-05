package com.sailor.net.okhttp;

import com.sailor.net.HttpDownCallback;

import java.util.Map;

public class HttpManager implements HttpClient {
    private HttpClient mClient;

    private HttpManager() {
        mClient = new OkHttpClient();
    }

    public static HttpManager getInstance() {
        return HttpManagerHolder.INSTANCE;
    }

    private static class HttpManagerHolder {
        private final static HttpManager INSTANCE = new HttpManager();
    }


    @Override
    public <T> void POST(String url, Map<String, Object> params, Class<T> clazz, HttpCallback httpCallback) {
        mClient.POST(url, params, clazz, httpCallback);
    }

    @Override
    public <T> void GET(String url, Class<T> clazz, HttpCallback httpCallback) {
        mClient.GET(url, clazz, httpCallback);
    }

    @Override
    public void DOWN(String url, String fileName, HttpDownCallback httpDownCallback) {
        mClient.DOWN(url, fileName, httpDownCallback);
    }
}
