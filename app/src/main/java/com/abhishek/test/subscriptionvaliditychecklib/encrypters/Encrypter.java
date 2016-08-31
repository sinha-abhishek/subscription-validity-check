package com.abhishek.test.subscriptionvaliditychecklib.encrypters;

import android.content.Context;

import com.abhishek.test.subscriptionvaliditychecklib.keymanagers.KeyManager;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public abstract class Encrypter {
    private String salt;
    private Context context;
    private KeyManager keyManager;


    public Encrypter(Context context, KeyManager keyManager, String salt) {
        this.context = context;
        this.keyManager = keyManager;
        this.salt = salt;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public KeyManager getKeyManager() {
        return keyManager;
    }

    public void setKeyManager(KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
    public abstract String getEncryptedString(String plainText) throws IOException, KeyChainException, CryptoInitializationException;
    public abstract String getDecryptedString(String cipherText) throws KeyChainException, CryptoInitializationException, IOException;
}
