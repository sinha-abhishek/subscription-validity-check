package com.abhishek.test.subscriptionvaliditychecklib;

import android.content.Context;
import android.content.SharedPreferences;

import com.abhishek.test.subscriptionvaliditychecklib.encrypters.ConcealEncrypter;
import com.abhishek.test.subscriptionvaliditychecklib.keymanagers.KeyManager;
import com.abhishek.test.subscriptionvaliditychecklib.keymanagers.SimpleKeyManager;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.junit.Test;
import org.junit.*;
import org.mockito.*;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SubscriptionValidityCheck.class, SimpleKeyManager.class, ConcealEncrypter.class, KeyManager.class})
public class SubscriptionValidityCheckTest {
    @Mock
    Context context;

    @Mock
    PreferenceUtil preferenceUtil;

    @Mock
    SharedPreferences sharedPreferences;

    @Mock
    SimpleKeyManager simpleKeyManager;

    @Mock
    ConcealEncrypter concealEncrypter;

    SubscriptionValidityCheck subscriptionValidityCheck;

    public SubscriptionValidityCheckTest() {

    }

    @Before
    public void setup() throws Exception {
        Mockito.when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);
        PowerMockito.whenNew(SimpleKeyManager.class).withAnyArguments().thenReturn(simpleKeyManager);
        PowerMockito.whenNew(ConcealEncrypter.class)
                .withArguments(context, simpleKeyManager, SubscriptionValidityCheck.SALT_STRING)
                .thenReturn(concealEncrypter);
        PowerMockito.doNothing().when(simpleKeyManager).initializeKey(context);
        Mockito.when(simpleKeyManager.getKey(context, SubscriptionValidityCheck.SALT_STRING)).thenReturn("key");
        subscriptionValidityCheck = new SubscriptionValidityCheck(context);
        subscriptionValidityCheck.setPreferenceUtil(preferenceUtil);
    }

    @Test
    public void testSubscribe() throws KeyChainException, CryptoInitializationException, IOException {
        String product = "xyz";
        long start = new Date().getTime() - 86400*1000*2; //current -2 days
        long server = new Date().getTime() - 2000; //current - 2sec
        long end = new Date().getTime() + 86400*1000*2; // current + 2 days;
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        Mockito.when(concealEncrypter.getEncryptedString(String.valueOf(start))).thenReturn("start");
        Mockito.when(concealEncrypter.getEncryptedString(String.valueOf(server))).thenReturn("server");
        Mockito.when(concealEncrypter.getEncryptedString(String.valueOf(end))).thenReturn("end");
        Mockito.when(concealEncrypter.getEncryptedString(String.valueOf(SubscriptionValidityCheck.STATE_ALLRIGHT))).thenReturn("allright");

        boolean val = subscriptionValidityCheck.startSubscription(product, start, end, server);
        Mockito.verify(preferenceUtil, times(1)).savePreference(PreferenceUtil.PREF_TYPE_STRING, "server_time", "server");
        Mockito.verify(preferenceUtil, times(1)).savePreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_end_time", "end");
        Mockito.verify(preferenceUtil, times(1)).savePreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_start_time", "start");
        Mockito.verify(preferenceUtil, times(1)).savePreference(eq(PreferenceUtil.PREF_TYPE_STRING), eq("local_time"), anyLong());
        Mockito.verify(preferenceUtil, times(1)).savePreference(PreferenceUtil.PREF_TYPE_STRING, "state_key", "allright");
        Assert.assertEquals(true, val);
    }

    @Test
    public void testSubscribeIncorrectDate() throws Exception {
        String product = "xyz";
        long start = new Date().getTime() - 86400 * 1000 * 2; //current -2 days
        long server = new Date().getTime() + 2000; //current + 2sec
        long end = new Date().getTime() + 86400 * 1000 * 2; // current + 2 days;
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());

        Mockito.when(concealEncrypter.getEncryptedString(String.valueOf(start))).thenReturn("start");
        Mockito.when(concealEncrypter.getEncryptedString(String.valueOf(server))).thenReturn("server");
        Mockito.when(concealEncrypter.getEncryptedString(String.valueOf(end))).thenReturn("end");
        Mockito.when(concealEncrypter.getEncryptedString(String.valueOf(SubscriptionValidityCheck.STATE_TIMECORRUPTED))).thenReturn("corrupt");

        boolean val = subscriptionValidityCheck.startSubscription(product, start, end, server);
        Assert.assertEquals(false, val);
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_STRING, "server_time", "server");
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_end_time", "end");
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_start_time", "start");
        Mockito.verify(preferenceUtil, times(0)).savePreference(eq(PreferenceUtil.PREF_TYPE_STRING), eq("local_time"), anyLong());
        Mockito.verify(preferenceUtil, times(1)).savePreference(PreferenceUtil.PREF_TYPE_STRING, "state_key", "corrupt");
    }

    @Test
    public void testSubscribeIncorrectEndTime() throws Exception {
        String product = "xyz";
        long end = new Date().getTime() - 86400 * 1000 * 2; //current -2 days
        long server = new Date().getTime() - 2000; //current + 2sec
        long start = new Date().getTime() + 86400 * 1000 * 2; // current + 2 days;
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        Mockito.when(concealEncrypter.getEncryptedString(String.valueOf(start))).thenReturn("start");
        Mockito.when(concealEncrypter.getEncryptedString(String.valueOf(server))).thenReturn("server");
        Mockito.when(concealEncrypter.getEncryptedString(String.valueOf(end))).thenReturn("end");
        boolean val = subscriptionValidityCheck.startSubscription(product, start, end, server);
        Assert.assertEquals(false, val);
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_STRING, "server_time", "server");
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_end_time", "end");
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_start_time", "start");
        Mockito.verify(preferenceUtil, times(0)).savePreference(eq(PreferenceUtil.PREF_TYPE_STRING), eq("local_time"), anyLong());
        Mockito.verify(preferenceUtil, times(0)).savePreference(eq(PreferenceUtil.PREF_TYPE_STRING), eq("state_key"), anyInt());
    }

    @Test
    public void testRefreshServerTimeValid() throws Exception {
        long server = new Date().getTime() - 2000;
        long testTime = server - 1000;
        Mockito.when(concealEncrypter.getEncryptedString(String.valueOf(server))).thenReturn("server");
        Mockito.when(concealEncrypter.getDecryptedString("testTime")).thenReturn(String.valueOf(testTime));
        //Mockito.when(concealEncrypter.getEncryptedString(String.valueOf(testTime))).thenReturn("test");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "server_time")).thenReturn("testTime");
        Mockito.when(concealEncrypter.getEncryptedString(String.valueOf(SubscriptionValidityCheck.STATE_ALLRIGHT))).thenReturn("allright");
        subscriptionValidityCheck.refreshServerTime(server);
        Mockito.verify(preferenceUtil, times(1)).savePreference(PreferenceUtil.PREF_TYPE_STRING, "server_time", "server");
        Mockito.verify(preferenceUtil, times(1)).savePreference(PreferenceUtil.PREF_TYPE_STRING, "state_key", "allright");
    }

    @Test
    public void testRefreshServerTimeInvalid() throws Exception {
        long server = new Date().getTime() - 2000;
        long testTime = server  + 1000;
        Mockito.when(concealEncrypter.getEncryptedString(String.valueOf(server))).thenReturn("server");
        Mockito.when(concealEncrypter.getDecryptedString("testTime")).thenReturn(String.valueOf(testTime));
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "server_time")).thenReturn("testTime");
        Mockito.when(concealEncrypter.getEncryptedString(String.valueOf(SubscriptionValidityCheck.STATE_ALLRIGHT))).thenReturn("allright");
        subscriptionValidityCheck.refreshServerTime(server);
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_LONG, "server_time", "server");
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_INTEGER, "state_key", "allright");
    }

    @Test
    public void testRefreshLocalTimeValid() throws Exception {
        long server = new Date().getTime() - 2000;
        long curLocal = server  + 1000;
        int state = SubscriptionValidityCheck.STATE_ALLRIGHT;
        Mockito.when(concealEncrypter.getDecryptedString("server")).thenReturn(String.valueOf(server));
        Mockito.when(concealEncrypter.getDecryptedString("curLocal")).thenReturn(String.valueOf(curLocal));
        Mockito.when(concealEncrypter.getDecryptedString("allright")).thenReturn(String.valueOf(state));
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "server_time")).thenReturn("server");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "local_time")).thenReturn("curLocal");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "state_key")).thenReturn("allright");
        subscriptionValidityCheck.refreshLocalTime();
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_STRING, "server_time", "server");
        Mockito.verify(preferenceUtil, times(0)).deletePreference("server_time");
        Mockito.verify(preferenceUtil, times(0)).savePreference(eq(PreferenceUtil.PREF_TYPE_STRING), eq("state_key"), anyString());
        Mockito.verify(preferenceUtil, times(1)).savePreference(eq(PreferenceUtil.PREF_TYPE_STRING), eq("local_time"), anyString());

    }

    @Test
    public void testRefreshLocalTimeInvalidServer() throws Exception {
        long server = new Date().getTime() + 2000;
        long curLocal = new Date().getTime()  - 1000;

        int state = SubscriptionValidityCheck.STATE_ALLRIGHT;
        Mockito.when(concealEncrypter.getDecryptedString("server")).thenReturn(String.valueOf(server));
        Mockito.when(concealEncrypter.getDecryptedString("curLocal")).thenReturn(String.valueOf(curLocal));
        Mockito.when(concealEncrypter.getDecryptedString("allright")).thenReturn(String.valueOf(state));
        Mockito.when(concealEncrypter.getEncryptedString(String.valueOf(SubscriptionValidityCheck.STATE_SERVERSYNCNEEDED))).thenReturn("sync");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "server_time")).thenReturn("server");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "local_time")).thenReturn("curLocal");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "state_key")).thenReturn("allright");
        subscriptionValidityCheck.refreshLocalTime();
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_STRING, "server_time", "server");
        Mockito.verify(preferenceUtil, times(1)).deletePreference("server_time");
        Mockito.verify(preferenceUtil, times(1)).savePreference(eq(PreferenceUtil.PREF_TYPE_STRING), eq("state_key"), eq("sync"));
        Mockito.verify(preferenceUtil, times(0)).savePreference(eq(PreferenceUtil.PREF_TYPE_STRING), eq("local_time"), anyString());

    }

    @Test
    public void testRefreshLocalTimeInvalidLocal() throws Exception {
        long server = new Date().getTime() - 2000;
        long curLocal = new Date().getTime()  + 2000;

        int state = SubscriptionValidityCheck.STATE_ALLRIGHT;
        Mockito.when(concealEncrypter.getDecryptedString("server")).thenReturn(String.valueOf(server));
        Mockito.when(concealEncrypter.getDecryptedString("curLocal")).thenReturn(String.valueOf(curLocal));
        Mockito.when(concealEncrypter.getDecryptedString("allright")).thenReturn(String.valueOf(state));
        Mockito.when(concealEncrypter.getEncryptedString(String.valueOf(SubscriptionValidityCheck.STATE_SERVERSYNCNEEDED))).thenReturn("sync");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "server_time")).thenReturn("server");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "local_time")).thenReturn("curLocal");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "state_key")).thenReturn("allright");
        subscriptionValidityCheck.refreshLocalTime();
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_STRING, "server_time", "server");
        Mockito.verify(preferenceUtil, times(1)).deletePreference("server_time");
        Mockito.verify(preferenceUtil, times(1)).savePreference(eq(PreferenceUtil.PREF_TYPE_STRING), eq("state_key"), eq("sync"));
        Mockito.verify(preferenceUtil, times(0)).savePreference(eq(PreferenceUtil.PREF_TYPE_STRING), eq("local_time"), anyString());

    }

    @Test
    public void testRefreshLocalTimeInvalidState() throws Exception {
        long server = new Date().getTime() - 2000;
        long curLocal = new Date().getTime()  - 1000;

        int state = SubscriptionValidityCheck.STATE_SERVERSYNCNEEDED;
        Mockito.when(concealEncrypter.getDecryptedString("server")).thenReturn(String.valueOf(server));
        Mockito.when(concealEncrypter.getDecryptedString("curLocal")).thenReturn(String.valueOf(curLocal));
        Mockito.when(concealEncrypter.getDecryptedString("sync")).thenReturn(String.valueOf(state));
        Mockito.when(concealEncrypter.getEncryptedString(String.valueOf(SubscriptionValidityCheck.STATE_SERVERSYNCNEEDED))).thenReturn("sync");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "server_time")).thenReturn("server");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "local_time")).thenReturn("curLocal");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "state_key")).thenReturn("sync");
        subscriptionValidityCheck.refreshLocalTime();
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_STRING, "server_time", "server");
        Mockito.verify(preferenceUtil, times(1)).deletePreference("server_time");
        Mockito.verify(preferenceUtil, times(1)).savePreference(eq(PreferenceUtil.PREF_TYPE_STRING), eq("state_key"), eq("sync"));
        Mockito.verify(preferenceUtil, times(0)).savePreference(eq(PreferenceUtil.PREF_TYPE_STRING), eq("local_time"), anyString());

    }

//    @Test
//    public void testRefreshSubscriptionValues() throws Exception {
//        SubscriptionValidityCheck spy = spy(subscriptionValidityCheck);
//        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
//        String product = "xyz";
//        long start = new Date().getTime() - 86400*1000*2; //current -2 days
//        long server = new Date().getTime() - 2000; //current - 2sec
//        long end = new Date().getTime() + 86400*1000*2; // current + 2 days;
//        Mockito.when(spy.startSubscription(product, start, end, server)).thenReturn(true);
//        spy.refreshSubscriptionValues(product, start, end, server);
//        Mockito.verify(spy, times(1)).startSubscription(product, start, end, server);
//    }

    @Test
    public void testSubscriptionValidForValid() throws Exception {
        String product = "xyz";
        long start = new Date().getTime() - 86400*1000*2; //current -2 days
        long server = new Date().getTime() - 2000; //current - 2sec
        long end = new Date().getTime() + 2000; // current + 2 sec;
        long curLocal = new Date().getTime();

        int state = SubscriptionValidityCheck.STATE_ALLRIGHT;
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "server_time")).thenReturn("server");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "local_time")).thenReturn("curLocal");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "state_key")).thenReturn("state");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_start_time")).thenReturn("start");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_end_time")).thenReturn("end");

        Mockito.when(concealEncrypter.getDecryptedString("server")).thenReturn(String.valueOf(server));
        Mockito.when(concealEncrypter.getDecryptedString("curLocal")).thenReturn(String.valueOf(curLocal));
        Mockito.when(concealEncrypter.getDecryptedString("state")).thenReturn(String.valueOf(state));
        Mockito.when(concealEncrypter.getDecryptedString("start")).thenReturn(String.valueOf(start));
        Mockito.when(concealEncrypter.getDecryptedString("end")).thenReturn(String.valueOf(end));

        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        //boolean val = subscriptionValidityCheck.startSubscription(product, start, end, server);
        boolean result = subscriptionValidityCheck.isSubscriptionValid(product);
        Assert.assertEquals(true, result);
//        Thread.sleep(3000);
//        boolean result2 = subscriptionValidityCheck.isSubscriptionValid(product);
//        Assert.assertEquals(false, result2);
    }

    @Test
    public void testSubscriptionValidForExpired() throws Exception {
        String product = "xyz";
        long start = new Date().getTime() - 86400*1000*2; //current -2 days
        long server = new Date().getTime() - 2000; //current - 2sec
        long end = new Date().getTime() + 2000; // current + 2 sec;
        long curLocal = new Date().getTime() + 3000;

        int state = SubscriptionValidityCheck.STATE_ALLRIGHT;
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "server_time")).thenReturn("server");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "local_time")).thenReturn("curLocal");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "state_key")).thenReturn("state");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_start_time")).thenReturn("start");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_end_time")).thenReturn("end");

        Mockito.when(concealEncrypter.getDecryptedString("server")).thenReturn(String.valueOf(server));
        Mockito.when(concealEncrypter.getDecryptedString("curLocal")).thenReturn(String.valueOf(curLocal));
        Mockito.when(concealEncrypter.getDecryptedString("state")).thenReturn(String.valueOf(state));
        Mockito.when(concealEncrypter.getDecryptedString("start")).thenReturn(String.valueOf(start));
        Mockito.when(concealEncrypter.getDecryptedString("end")).thenReturn(String.valueOf(end));
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        //boolean val = subscriptionValidityCheck.startSubscription(product, start, end, server);
        boolean result = subscriptionValidityCheck.isSubscriptionValid(product);
        Assert.assertEquals(false, result);

    }

    @Test
    public void testSubscriptionValidForLocalBeforeStart() throws Exception {
        String product = "xyz";
        long start = new Date().getTime() - 86400*1000*2; //current -2 days
        long server = new Date().getTime() - 2000; //current - 2sec
        long end = new Date().getTime() + 2000; // current + 2 sec;
        long curLocal = start - 3000;

        int state = SubscriptionValidityCheck.STATE_ALLRIGHT;
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "server_time")).thenReturn("server");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "local_time")).thenReturn("curLocal");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "state_key")).thenReturn("state");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_start_time")).thenReturn("start");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_end_time")).thenReturn("end");

        Mockito.when(concealEncrypter.getDecryptedString("server")).thenReturn(String.valueOf(server));
        Mockito.when(concealEncrypter.getDecryptedString("curLocal")).thenReturn(String.valueOf(curLocal));
        Mockito.when(concealEncrypter.getDecryptedString("state")).thenReturn(String.valueOf(state));
        Mockito.when(concealEncrypter.getDecryptedString("start")).thenReturn(String.valueOf(start));
        Mockito.when(concealEncrypter.getDecryptedString("end")).thenReturn(String.valueOf(end));
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        //boolean val = subscriptionValidityCheck.startSubscription(product, start, end, server);
        boolean result = subscriptionValidityCheck.isSubscriptionValid(product);
        Assert.assertEquals(true, result);

    }

    @Test
    public void testSubscriptionValidForInvalidServerBeforeStart() throws Exception {
        String product = "xyz";
        long start = new Date().getTime() - 86400*1000*2; //current -2 days
        long server = new Date().getTime() - 2000; //current - 2sec
        long end = new Date().getTime() + 2000; // current + 2 sec;
        long curLocal = end + 2000;

        int state = SubscriptionValidityCheck.STATE_ALLRIGHT;
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "server_time")).thenReturn("server");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "local_time")).thenReturn("curLocal");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "state_key")).thenReturn("state");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_start_time")).thenReturn("start");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_end_time")).thenReturn("end");

        Mockito.when(concealEncrypter.getDecryptedString("server")).thenReturn(String.valueOf(server));
        Mockito.when(concealEncrypter.getDecryptedString("curLocal")).thenReturn(String.valueOf(curLocal));
        Mockito.when(concealEncrypter.getDecryptedString("state")).thenReturn(String.valueOf(state));
        Mockito.when(concealEncrypter.getDecryptedString("start")).thenReturn(String.valueOf(start));
        Mockito.when(concealEncrypter.getDecryptedString("end")).thenReturn(String.valueOf(end));
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        //boolean val = subscriptionValidityCheck.startSubscription(product, start, end, server);
        boolean result = subscriptionValidityCheck.isSubscriptionValid(product);
        Assert.assertEquals(false, result);

    }

    @Test
    public void testSubscriptionValidForServerBeforeStart() throws Exception {
        String product = "xyz";
        long start = new Date().getTime() - 86400*1000*2; //current -2 days
        long server = start - 2000; //current - 2sec
        long end = new Date().getTime() + 2000; // current + 2 sec;
        long curLocal = new Date().getTime();

        int state = SubscriptionValidityCheck.STATE_ALLRIGHT;
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "server_time")).thenReturn("server");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "local_time")).thenReturn("curLocal");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "state_key")).thenReturn("state");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_start_time")).thenReturn("start");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_end_time")).thenReturn("end");

        Mockito.when(concealEncrypter.getDecryptedString("server")).thenReturn(String.valueOf(server));
        Mockito.when(concealEncrypter.getDecryptedString("curLocal")).thenReturn(String.valueOf(curLocal));
        Mockito.when(concealEncrypter.getDecryptedString("state")).thenReturn(String.valueOf(state));
        Mockito.when(concealEncrypter.getDecryptedString("start")).thenReturn(String.valueOf(start));
        Mockito.when(concealEncrypter.getDecryptedString("end")).thenReturn(String.valueOf(end));
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        //boolean val = subscriptionValidityCheck.startSubscription(product, start, end, server);
        boolean result = subscriptionValidityCheck.isSubscriptionValid(product);
        Assert.assertEquals(true, result);

    }

    @Test
    public void testSubscriptionValidForInvalidState() throws Exception {
        String product = "xyz";
        long start = new Date().getTime() - 86400*1000*2; //current -2 days
        long server = new Date().getTime() - 2000; //current - 2sec
        long end = new Date().getTime() + 2000; // current + 2 sec;
        long curLocal = start - 3000;

        int state = SubscriptionValidityCheck.STATE_SERVERSYNCNEEDED;
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "server_time")).thenReturn("server");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "local_time")).thenReturn("curLocal");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "state_key")).thenReturn("state");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_start_time")).thenReturn("start");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_end_time")).thenReturn("end");

        Mockito.when(concealEncrypter.getDecryptedString("server")).thenReturn(String.valueOf(server));
        Mockito.when(concealEncrypter.getDecryptedString("curLocal")).thenReturn(String.valueOf(curLocal));
        Mockito.when(concealEncrypter.getDecryptedString("state")).thenReturn(String.valueOf(state));
        Mockito.when(concealEncrypter.getDecryptedString("start")).thenReturn(String.valueOf(start));
        Mockito.when(concealEncrypter.getDecryptedString("end")).thenReturn(String.valueOf(end));
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        //boolean val = subscriptionValidityCheck.startSubscription(product, start, end, server);
        boolean result = subscriptionValidityCheck.isSubscriptionValid(product);
        Assert.assertEquals(false, result);

    }

    @Test
    public void testSubscriptionValidForInvalidState2() throws Exception {
        String product = "xyz";
        long start = new Date().getTime() - 86400*1000*2; //current -2 days
        long server = new Date().getTime() - 2000; //current - 2sec
        long end = new Date().getTime() + 2000; // current + 2 sec;
        long curLocal = start - 3000;

        int state = SubscriptionValidityCheck.STATE_TIMECORRUPTED;
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "server_time")).thenReturn("server");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "local_time")).thenReturn("curLocal");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "state_key")).thenReturn("state");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_start_time")).thenReturn("start");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_end_time")).thenReturn("end");

        Mockito.when(concealEncrypter.getDecryptedString("server")).thenReturn(String.valueOf(server));
        Mockito.when(concealEncrypter.getDecryptedString("curLocal")).thenReturn(String.valueOf(curLocal));
        Mockito.when(concealEncrypter.getDecryptedString("state")).thenReturn(String.valueOf(state));
        Mockito.when(concealEncrypter.getDecryptedString("start")).thenReturn(String.valueOf(start));
        Mockito.when(concealEncrypter.getDecryptedString("end")).thenReturn(String.valueOf(end));
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        //boolean val = subscriptionValidityCheck.startSubscription(product, start, end, server);
        boolean result = subscriptionValidityCheck.isSubscriptionValid(product);
        Assert.assertEquals(false, result);

    }

    @Test
    public void testSubscriptionValidServerInvalid() throws Exception {
        String product = "xyz";
        long start = new Date().getTime() - 86400*1000*2; //current -2 days
        long server = 0; //current - 2sec
        long end = new Date().getTime() + 2000; // current + 2 sec;
        long curLocal = new Date().getTime();

        int state = SubscriptionValidityCheck.STATE_ALLRIGHT;
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "server_time")).thenReturn("server");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "local_time")).thenReturn("curLocal");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "state_key")).thenReturn("state");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_start_time")).thenReturn("start");
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_STRING, "xyz_end_time")).thenReturn("end");

        Mockito.when(concealEncrypter.getDecryptedString("server")).thenReturn(String.valueOf(server));
        Mockito.when(concealEncrypter.getDecryptedString("curLocal")).thenReturn(String.valueOf(curLocal));
        Mockito.when(concealEncrypter.getDecryptedString("state")).thenReturn(String.valueOf(state));
        Mockito.when(concealEncrypter.getDecryptedString("start")).thenReturn(String.valueOf(start));
        Mockito.when(concealEncrypter.getDecryptedString("end")).thenReturn(String.valueOf(end));
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        //boolean val = subscriptionValidityCheck.startSubscription(product, start, end, server);
        boolean result = subscriptionValidityCheck.isSubscriptionValid(product);
        Assert.assertEquals(false, result);

    }



}
