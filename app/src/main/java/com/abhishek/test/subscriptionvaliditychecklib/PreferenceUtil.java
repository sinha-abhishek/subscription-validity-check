package com.abhishek.test.subscriptionvaliditychecklib;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Set;

public class PreferenceUtil {
    private Context context;
    public static final String PREFERENCES = "validitypreferences";
    public static final int PREF_TYPE_INTEGER = 0;
    public static final int PREF_TYPE_STRING = 1;
    public static final int PREF_TYPE_BOOLEAN = 2;
    public static final int PREF_TYPE_FLOAT = 3;
    public static final int PREF_TYPE_LONG = 4;
    public static final int PREF_TYPE_STRINGSET = 5;

    private SharedPreferences prefs;

    public PreferenceUtil(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    /**
     * Save shared preference.
     *
     * @param prefType
     * @param key
     * @param value
     */
    public void savePreference(int prefType, String key, Object value) {

        try {
            SharedPreferences.Editor editor = prefs.edit();
            switch (prefType) {
                case PREF_TYPE_INTEGER:
                    editor.putInt(key, (Integer) value);
                    break;

                case PREF_TYPE_STRING:
                    editor.putString(key, (String) value);
                    break;

                case PREF_TYPE_BOOLEAN:
                    editor.putBoolean(key, (Boolean) value);
                    break;

                case PREF_TYPE_FLOAT:
                    editor.putFloat(key, (Float) value);
                    break;

                case PREF_TYPE_LONG:
                    editor.putLong(key, (Long) value);
                    break;

                case PREF_TYPE_STRINGSET:
                    editor.putStringSet(key, (Set<String>) value);
                    break;
                default:
                    break;
            }
            editor.commit();
        } catch (Exception e) {
            Log.e(PreferenceUtil.class.getSimpleName(), "Unable to store data to Shared preferences");
        }
    }

    /**
     * Get shared preference.
     *
     * @param key
     * @param prefType
     * @return
     */
    public Object getPreference(int prefType, String key) {

        Object value ;
        switch (prefType) {
            case PREF_TYPE_INTEGER:
                value = prefs.getInt(key, 0);
                break;
            case PREF_TYPE_STRING:
                value = prefs.getString(key, null);
                break;
            case PREF_TYPE_BOOLEAN:
                value = prefs.getBoolean(key, false);
                break;
            case PREF_TYPE_FLOAT:
                value = prefs.getFloat(key, 0);
                break;
            case PREF_TYPE_LONG:
                value = prefs.getLong(key, 0);
                break;
            case PREF_TYPE_STRINGSET:
                value = prefs.getStringSet(key,null);
                break;
            default:
                value = null;
        }
        return value;
    }

    public void deletePreference(String prefKey) {
        if (prefs.contains(prefKey))
            prefs.edit().remove(prefKey).apply();
    }

    public SharedPreferences getSharedPref() {
        return prefs;
    }

    public boolean getBoolean(String booleanKey) {
        return prefs.getBoolean(booleanKey, false);
    }
}
