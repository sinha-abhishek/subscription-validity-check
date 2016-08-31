package com.abhishek.test.subscriptionvaliditychecklib;

import android.content.Context;

import com.abhishek.test.subscriptionvaliditychecklib.encrypters.ConcealEncrypter;
import com.abhishek.test.subscriptionvaliditychecklib.encrypters.Encrypter;
import com.abhishek.test.subscriptionvaliditychecklib.keymanagers.KeyManager;
import com.abhishek.test.subscriptionvaliditychecklib.keymanagers.SimpleKeyManager;

import java.util.Date;

public class SubscriptionValidityCheck {
    private Context _context;
    private PreferenceUtil preferenceUtil;

    private static String END_TIME = "end_time";
    private static String START_TIME = "start_time";
    private static String SERVER_TIME = "server_time";
    private static String LOCAL_TIME = "local_time";
    private static String PREF_STATE_KEY = "state_key";

    public static int STATE_ALLRIGHT = 0;
    public static int STATE_TIMECORRUPTED = 1;
    public static int STATE_SERVERSYNCNEEDED = 2;

    //TODO: move these to so or buildconfig
    public static String SALT_STRING = "saltstring";

    private KeyManager keyManager;
    private Encrypter encrypter;

    public SubscriptionValidityCheck(Context context) {
        _context = context;
        preferenceUtil = new PreferenceUtil(context);
        //TODO: make these factory based
        keyManager = new SimpleKeyManager();
        keyManager.initializeKey(context);
        encrypter = new ConcealEncrypter(context, keyManager, SALT_STRING);

    }

    public void setPreferenceUtil(PreferenceUtil preferenceUtil) {
        this.preferenceUtil = preferenceUtil;
    }

    public boolean startSubscription(String product, long startTimeStamp, long endTimeStamp, long serverTimeStamp) {
        Date date = new Date();
        Date serverTime = new Date(serverTimeStamp);
        if (date.before(serverTime)) {
            setState(STATE_TIMECORRUPTED);
            return false;
        }
        if (endTimeStamp < startTimeStamp) {
            return  false;
        }
        //TODO: store preferences
        setServerTime(serverTimeStamp);
        setLocalTime(date.getTime());
        setEndTime(product, endTimeStamp);
        setStartTime(product, startTimeStamp);
        setState(STATE_ALLRIGHT);
        return true;
    }

    public void refreshServerTime(long serverTimeStamp) {
        long curServerTime = getServerTime();
        if (curServerTime < serverTimeStamp) {
            setServerTime(serverTimeStamp);
            setState(STATE_ALLRIGHT);
        }
    }

    private long getServerTime() {
        return (long)preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_LONG, SERVER_TIME);
    }

    private long getLocalTime() {
        return (long) preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_LONG, LOCAL_TIME);
    }

    private void setServerTime(long serverTimeStamp) {
        preferenceUtil.savePreference(PreferenceUtil.PREF_TYPE_LONG, SERVER_TIME, serverTimeStamp);
    }

    private void setLocalTime(long localTime) {
        preferenceUtil.savePreference(PreferenceUtil.PREF_TYPE_LONG, LOCAL_TIME, localTime);
    }

    private long getEndTime(String product) {
        return (long)preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_LONG, getEndTimeKey(product));
    }

    private void setEndTime(String product, long timestamp) {
        preferenceUtil.savePreference(PreferenceUtil.PREF_TYPE_LONG, getEndTimeKey(product), timestamp);
    }

    private long getStartTime(String product) {
        return (long)preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_LONG, getStartTimeKey(product));
    }

    private void setStartTime(String product, long timestamp) {
        preferenceUtil.savePreference(PreferenceUtil.PREF_TYPE_LONG, getStartTimeKey(product), timestamp);
    }

    private boolean detectDiscrepancy(long currentTimeStamp) {
        if (getState() == STATE_TIMECORRUPTED ||
                getState() == STATE_SERVERSYNCNEEDED) {
            return true;
        }
        long serverTime = getServerTime();
        long lastSavedLocalTime = getLocalTime();
        return currentTimeStamp < serverTime || currentTimeStamp < lastSavedLocalTime;
    }

    public void refreshLocalTime() {
        long currentSystemTime = new Date().getTime();
        if (detectDiscrepancy(currentSystemTime)) {
            preferenceUtil.deletePreference(SERVER_TIME);
            setState(STATE_SERVERSYNCNEEDED);
        } else {
            setLocalTime(currentSystemTime);
        }
    }

    public boolean refreshSubscriptionValues(String product, long startTimeStamp, long endTimeStamp, long serverTimeStamp) {
        return startSubscription(product, startTimeStamp, endTimeStamp, serverTimeStamp);
    }

    public boolean isSubscriptionValid(String product) {
        refreshLocalTime();
        if (getState() != STATE_ALLRIGHT) {
            return false;
        }
        if (getServerTime() <= 0) {
            return false;
        }
        long serverTime = getServerTime();
        long lastSavedLocalTime = getLocalTime();
        long endTime = getEndTime(product);
        long startTime = getStartTime(product);
        long timeToCompare = serverTime > lastSavedLocalTime ? serverTime : lastSavedLocalTime;
        return timeToCompare >= startTime &&
                timeToCompare <= endTime;
    }

    public int getState() {
        return (int)preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_INTEGER, PREF_STATE_KEY);
    }

    public void setState(int state) {
        preferenceUtil.savePreference(PreferenceUtil.PREF_TYPE_INTEGER, PREF_STATE_KEY, state);
    }

    protected String getEndTimeKey(String product) {
        return product+ "_"+ END_TIME;
    }

    protected String getStartTimeKey(String product) {
        return product + "_" + START_TIME;
    }
}
