package com.abhishek.test.subscriptionvaliditychecklib.keymanagers;

import android.content.Context;

public interface KeyManager {
    void initializeKey(Context context);
    boolean IsKeyCreated(Context context);
    String getKey(Context context, String salt);
}
