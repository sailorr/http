package com.sailor.net.retrofit;

import com.sailor.net.HttpDownCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.functions.Consumer;
import okhttp3.ResponseBody;

/**
 * @author sailor on2019/6/4 16:11
 * @desc 重写 Consumer<T>
 */
public class DownConsumer implements Consumer {
    private HttpDownCallback mDownCallback;
    private String filename;

    public DownConsumer(String fileName, HttpDownCallback downCallback) {
        this.mDownCallback = downCallback;
        this.filename = fileName;
    }

    @Override
    public void accept(Object o) throws Exception {
        ResponseBody body = (ResponseBody) o;
        InputStream inputStream = null;
        byte[] buffer = new byte[2048];
        int len;
        try {
            //必须先创建文件夹，动态权限。
            File file = new File(filename);
            FileOutputStream outputStream = new FileOutputStream(file);
            inputStream = body.byteStream();
            long total = body.contentLength();
            long sum = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
                sum += len;
                int progress = (int) (sum * 1.0f / total * 100);
                mDownCallback.onProgress(progress);
            }
            outputStream.flush();
            mDownCallback.onSuccess(file);
        } catch (IOException e) {
            e.printStackTrace();
            mDownCallback.onError(e);
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


}
