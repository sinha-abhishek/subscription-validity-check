package com.abhishek.test.subscriptionvaliditychecklib.encrypters;

import android.content.Context;
import android.util.Base64;

import com.abhishek.test.subscriptionvaliditychecklib.keymanagers.KeyManager;
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class ConcealEncrypter extends Encrypter{
    private Crypto _crypto;
    private Entity _entity;

    public ConcealEncrypter(Context context, KeyManager keyManager, String salt) {
        super(context, keyManager, salt);
        _crypto = new Crypto(new SharedPrefsBackedKeyChain(context),
                new SystemNativeCryptoLibrary());
        _entity = new Entity(salt);
    }


    @Override
    public String getEncryptedString(String plainText) throws IOException, KeyChainException, CryptoInitializationException {
        byte[] cipher = _crypto.encrypt(plainText.getBytes("UTF-8"), _entity);
        String cipherText = new String(cipher,"UTF-8");
        String base64 = Base64.encodeToString(cipher, Base64.DEFAULT);
        return base64;

    }

    @Override
    public String getDecryptedString(String cipherText) throws KeyChainException, CryptoInitializationException, IOException {
        byte[] data = Base64.decode(cipherText, Base64.DEFAULT);
        byte[] cipher = _crypto.decrypt(data, _entity);
        String plain = new String(cipher,"UTF-8");
        return plain;
    }
}
