package com.abhishek.test.subscriptionvaliditychecklib;

import android.content.Context;
import android.content.SharedPreferences;

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

import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({SubscriptionValidityCheck.class})
public class SubscriptionValidityCheckTest {
    @Mock
    Context context;

    @Mock
    PreferenceUtil preferenceUtil;

    @Mock
    SharedPreferences sharedPreferences;
    SubscriptionValidityCheck subscriptionValidityCheck;

    public SubscriptionValidityCheckTest() {

    }

    @Before
    public void setup() {
        Mockito.when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);
        subscriptionValidityCheck = new SubscriptionValidityCheck(context);
        subscriptionValidityCheck.setPreferenceUtil(preferenceUtil);
    }

    @Test
    public void testSubscribe() {
        String product = "xyz";
        long start = new Date().getTime() - 86400*1000*2; //current -2 days
        long server = new Date().getTime() - 2000; //current - 2sec
        long end = new Date().getTime() + 86400*1000*2; // current + 2 days;
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        boolean val = subscriptionValidityCheck.startSubscription(product, start, end, server);
        Mockito.verify(preferenceUtil, times(1)).savePreference(PreferenceUtil.PREF_TYPE_LONG, "server_time", server);
        Mockito.verify(preferenceUtil, times(1)).savePreference(PreferenceUtil.PREF_TYPE_LONG, "xyz_end_time", end);
        Mockito.verify(preferenceUtil, times(1)).savePreference(PreferenceUtil.PREF_TYPE_LONG, "xyz_start_time", start);
        Mockito.verify(preferenceUtil, times(1)).savePreference(eq(PreferenceUtil.PREF_TYPE_LONG), eq("local_time"), anyLong());
        Mockito.verify(preferenceUtil, times(1)).savePreference(PreferenceUtil.PREF_TYPE_INTEGER, "state_key", SubscriptionValidityCheck.STATE_ALLRIGHT);
        Assert.assertEquals(true, val);
    }

    @Test
    public void testSubscribeIncorrectDate() throws Exception {
        String product = "xyz";
        long start = new Date().getTime() - 86400 * 1000 * 2; //current -2 days
        long server = new Date().getTime() + 2000; //current + 2sec
        long end = new Date().getTime() + 86400 * 1000 * 2; // current + 2 days;
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        boolean val = subscriptionValidityCheck.startSubscription(product, start, end, server);
        Assert.assertEquals(false, val);
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_LONG, "server_time", server);
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_LONG, "xyz_end_time", end);
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_LONG, "xyz_start_time", start);
        Mockito.verify(preferenceUtil, times(0)).savePreference(eq(PreferenceUtil.PREF_TYPE_LONG), eq("local_time"), anyLong());
        Mockito.verify(preferenceUtil, times(1)).savePreference(PreferenceUtil.PREF_TYPE_INTEGER, "state_key", SubscriptionValidityCheck.STATE_TIMECORRUPTED);
    }

    @Test
    public void testSubscribeIncorrectEndTime() throws Exception {
        String product = "xyz";
        long end = new Date().getTime() - 86400 * 1000 * 2; //current -2 days
        long server = new Date().getTime() - 2000; //current + 2sec
        long start = new Date().getTime() + 86400 * 1000 * 2; // current + 2 days;
        Mockito.doNothing().when(preferenceUtil).savePreference(anyInt(), anyString(), anyObject());
        boolean val = subscriptionValidityCheck.startSubscription(product, start, end, server);
        Assert.assertEquals(false, val);
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_LONG, "server_time", server);
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_LONG, "xyz_end_time", end);
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_LONG, "xyz_start_time", start);
        Mockito.verify(preferenceUtil, times(0)).savePreference(eq(PreferenceUtil.PREF_TYPE_LONG), eq("local_time"), anyLong());
        Mockito.verify(preferenceUtil, times(0)).savePreference(eq(PreferenceUtil.PREF_TYPE_INTEGER), eq("state_key"), anyInt());
    }

    @Test
    public void testRefreshServerTimeValid() throws Exception {
        long server = new Date().getTime() - 2000;
        long testTime = server - 1000;
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_LONG, "server_time")).thenReturn(testTime);
        subscriptionValidityCheck.refreshServerTime(server);
        Mockito.verify(preferenceUtil, times(1)).savePreference(PreferenceUtil.PREF_TYPE_LONG, "server_time", server);
        Mockito.verify(preferenceUtil, times(1)).savePreference(PreferenceUtil.PREF_TYPE_INTEGER, "state_key", SubscriptionValidityCheck.STATE_ALLRIGHT);
    }

    @Test
    public void testRefreshServerTimeInvalid() throws Exception {
        long server = new Date().getTime() - 2000;
        long testTime = server  + 1000;
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_LONG, "server_time")).thenReturn(testTime);
        subscriptionValidityCheck.refreshServerTime(server);
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_LONG, "server_time", server);
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_INTEGER, "state_key", SubscriptionValidityCheck.STATE_ALLRIGHT);
    }

    @Test
    public void testRefreshLocalTimeValid() throws Exception {
        long server = new Date().getTime() - 2000;
        long curLocal = server  + 1000;
        int state = SubscriptionValidityCheck.STATE_ALLRIGHT;
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_LONG, "server_time")).thenReturn(server);
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_LONG, "local_time")).thenReturn(curLocal);
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_INTEGER, "state_key")).thenReturn(state);
        subscriptionValidityCheck.refreshLocalTime();
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_LONG, "server_time", server);
        Mockito.verify(preferenceUtil, times(0)).deletePreference("server_time");
        Mockito.verify(preferenceUtil, times(0)).savePreference(eq(PreferenceUtil.PREF_TYPE_INTEGER), eq("state_key"), anyInt());
        Mockito.verify(preferenceUtil, times(1)).savePreference(eq(PreferenceUtil.PREF_TYPE_LONG), eq("local_time"), anyLong());

    }

    @Test
    public void testRefreshLocalTimeInvalidServer() throws Exception {
        long server = new Date().getTime() + 2000;
        long curLocal = new Date().getTime()  - 1000;

        int state = SubscriptionValidityCheck.STATE_ALLRIGHT;
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_LONG, "server_time")).thenReturn(server);
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_LONG, "local_time")).thenReturn(curLocal);
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_INTEGER, "state_key")).thenReturn(state);
        subscriptionValidityCheck.refreshLocalTime();
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_LONG, "server_time", server);
        Mockito.verify(preferenceUtil, times(1)).deletePreference("server_time");
        Mockito.verify(preferenceUtil, times(1)).savePreference(eq(PreferenceUtil.PREF_TYPE_INTEGER), eq("state_key"), eq(SubscriptionValidityCheck.STATE_SERVERSYNCNEEDED));
        Mockito.verify(preferenceUtil, times(0)).savePreference(eq(PreferenceUtil.PREF_TYPE_LONG), eq("local_time"), anyLong());

    }

    @Test
    public void testRefreshLocalTimeInvalidLocal() throws Exception {
        long server = new Date().getTime() - 2000;
        long curLocal = new Date().getTime()  + 2000;

        int state = SubscriptionValidityCheck.STATE_ALLRIGHT;
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_LONG, "server_time")).thenReturn(server);
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_LONG, "local_time")).thenReturn(curLocal);
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_INTEGER, "state_key")).thenReturn(state);
        subscriptionValidityCheck.refreshLocalTime();
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_LONG, "server_time", server);
        Mockito.verify(preferenceUtil, times(1)).deletePreference("server_time");
        Mockito.verify(preferenceUtil, times(1)).savePreference(eq(PreferenceUtil.PREF_TYPE_INTEGER), eq("state_key"), eq(SubscriptionValidityCheck.STATE_SERVERSYNCNEEDED));
        Mockito.verify(preferenceUtil, times(0)).savePreference(eq(PreferenceUtil.PREF_TYPE_LONG), eq("local_time"), anyLong());

    }

    @Test
    public void testRefreshLocalTimeInvalidState() throws Exception {
        long server = new Date().getTime() - 2000;
        long curLocal = new Date().getTime()  - 1000;

        int state = SubscriptionValidityCheck.STATE_SERVERSYNCNEEDED;
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_LONG, "server_time")).thenReturn(server);
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_LONG, "local_time")).thenReturn(curLocal);
        Mockito.when(preferenceUtil.getPreference(PreferenceUtil.PREF_TYPE_INTEGER, "state_key")).thenReturn(state);
        subscriptionValidityCheck.refreshLocalTime();
        Mockito.verify(preferenceUtil, times(0)).savePreference(PreferenceUtil.PREF_TYPE_LONG, "server_time", server);
        Mockito.verify(preferenceUtil, times(1)).deletePreference("server_time");
        Mockito.verify(preferenceUtil, times(1)).savePreference(eq(PreferenceUtil.PREF_TYPE_INTEGER), eq("state_key"), eq(SubscriptionValidityCheck.STATE_SERVERSYNCNEEDED));
        Mockito.verify(preferenceUtil, times(0)).savePreference(eq(PreferenceUtil.PREF_TYPE_LONG), eq("local_time"), anyLong());

    }

}
