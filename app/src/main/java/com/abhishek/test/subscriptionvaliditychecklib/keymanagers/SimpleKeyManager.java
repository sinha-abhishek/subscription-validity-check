package com.abhishek.test.subscriptionvaliditychecklib.keymanagers;

import android.content.Context;
import android.provider.Settings;
import android.util.Base64;

import com.abhishek.test.subscriptionvaliditychecklib.PreferenceUtil;

import java.security.SecureRandom;

public class SimpleKeyManager implements KeyManager {
    //TODO: keep secret in buildconfig or NDK
    //APP should enable proguard
    private static String SECRET = "YOUR_SECRET";
    private static final String PREF_KEY = "offline_key_part";
    @Override
    public void initializeKey(Context context) {
        SecureRandom random = new SecureRandom();
        byte randomBytes[] = new byte[20];
        random.nextBytes(randomBytes);
        String part1 = Base64.encodeToString(randomBytes, Base64.DEFAULT);
        PreferenceUtil appPreferenceHelper = new PreferenceUtil(context);
        if (appPreferenceHelper.getSharedPref().contains(PREF_KEY)) {
            return;
        }
        appPreferenceHelper.savePreference(PreferenceUtil.PREF_TYPE_STRING, PREF_KEY, part1);
    }

    private String makeKey(Context context, String part1, String salt) {
        String secureId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        //TODO: find a better algortihm for making key
        String keyString = SECRET + part1 + secureId + salt;
        String key = Base64.encodeToString(keyString.getBytes(), Base64.DEFAULT);
        //TODO: sha1 string maybe
        return key;
    }


    @Override
    public boolean IsKeyCreated(Context context) {
        PreferenceUtil appPreferenceHelper = new PreferenceUtil(context);
        if (appPreferenceHelper.getSharedPref().contains(PREF_KEY)) {
            return true;
        }
        return false;
    }

    @Override
    public String getKey(Context context, String salt) {
        String part1 = (String)(new PreferenceUtil(context)).getPreference(PreferenceUtil.PREF_TYPE_STRING, PREF_KEY);
        String key = makeKey(context, part1,salt);
        return key;
    }
}
