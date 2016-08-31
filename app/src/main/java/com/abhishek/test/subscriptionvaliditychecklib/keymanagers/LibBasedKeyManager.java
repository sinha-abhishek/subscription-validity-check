package com.abhishek.test.subscriptionvaliditychecklib.keymanagers;

import android.content.Context;

public class LibBasedKeyManager implements KeyManager {
    @Override
    public void initializeKey(Context context) {

    }

    @Override
    public boolean IsKeyCreated(Context context) {
        return false;
    }

    @Override
    public String getKey(Context context, String salt) {
        return null;
    }
}
