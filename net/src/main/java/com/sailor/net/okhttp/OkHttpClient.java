package com.sailor.net.okhttp;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sailor.net.HttpDownCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttpClient implements HttpClient {

    private okhttp3.OkHttpClient mClient;

    private Map<String, List<Cookie>> cookieStore = new HashMap<>();

    public OkHttpClient() {
        okhttp3.OkHttpClient.Builder builder = new okhttp3.OkHttpClient.Builder();

        //缓存
        builder.cookieJar(new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                cookieStore.put(url.host(), cookies);

            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                List<Cookie> cookies = cookieStore.get(url.host());
                return cookies != null ? cookies : new ArrayList<Cookie>();
            }
        });
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);
        builder.addInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.i("okHttp", message);
            }
        }).setLevel(HttpLoggingInterceptor.Level.BODY));
        mClient = builder.build();
    }


    /**
     * response处理
     *
     * @param callback
     * @param response
     * @param clazz
     * @param <T>
     */
    private <T> void processResponse(HttpCallback callback, Response response, Class<T> clazz) {
        if (response.code() == 200) {
            try {
                assert response.body() != null;
                String body = response.body().string();
                if (String.class == clazz) {
                    callback.onSuccess(body);
                    return;
                }
                T tClass = new Gson().fromJson(body, clazz);
                callback.onSuccess(tClass);
            } catch (JsonSyntaxException e) {
                callback.onError(e);
            } catch (Exception e) {
                e.printStackTrace();
                callback.onError(e);
            }

        } else {
            callback.onFail(response.code());
        }
    }

    /**
     * POST方法
     *
     * @param url          请求地址
     * @param params       请求参数
     * @param clazz        需要转化的实体类
     * @param httpCallback 回调函数
     * @param <T>
     */

    @Override
    public <T> void POST(String url, Map<String, Object> params, final Class<T> clazz, final HttpCallback httpCallback) {
        StringBuilder content = new StringBuilder();
        if (params != null) {
            for (Map.Entry entry : params.entrySet()) {
                content.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        if (!TextUtils.isEmpty(content.toString())) {
            content = new StringBuilder(content.substring(0, content.lastIndexOf("&")));
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), content.toString());
        Request request = new Request
                .Builder()
                .url(url)
                .post(body)
                .build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                httpCallback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                processResponse(httpCallback, response, clazz);
            }
        });
    }


    /**
     * GET方法
     *
     * @param url          请求地址
     * @param clazz        需要转化的实体类
     * @param httpCallback 回调函数
     * @param <T>
     */

    @Override
    public <T> void GET(String url, final Class<T> clazz, final HttpCallback httpCallback) {
        //建立okhttp request
        Request request = new Request.Builder().url(url).build();
        //将request加入call
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                httpCallback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                processResponse(httpCallback, response, clazz);
            }
        });
    }

    @Override
    public void DOWN(String url, final String filename, final HttpDownCallback httpDownCallback) {
        //建立okhttp request
        Request request = new Request.Builder().url(url).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                httpDownCallback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                InputStream inputStream = null;
                byte[] buffer = new byte[2048];
                int len;

                try {
                    File file = new File(filename);
                    FileOutputStream outputStream = new FileOutputStream(file);
                    assert response.body() != null;
                    inputStream = response.body().byteStream();
                    long total = response.body().contentLength();
                    long sum = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        httpDownCallback.onProgress(progress);
                    }
                    outputStream.flush();
                    httpDownCallback.onSuccess(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    httpDownCallback.onError(e);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                }
            }
        });
    }
}
