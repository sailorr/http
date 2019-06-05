package com.sailor.net;

import java.io.File;

public interface HttpDownCallback {
    void onProgress(int progress);

    void onSuccess(File file);

    void onFailure(int responseCode);

    void onError(Exception e);
}
