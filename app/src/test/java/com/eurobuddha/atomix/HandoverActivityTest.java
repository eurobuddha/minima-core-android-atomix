package com.eurobuddha.atomix;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

/** 0.1.89: only the real Dexicon (same signing key) may ask, and an already-handed-over AtomiX re-confirms at once
 *  when every check still passes — Dexicon asks again before every uninstall instead of storing the OK. */
@RunWith(RobolectricTestRunner.class) @Config(sdk = 33)
public class HandoverActivityTest {
    private static final Signature FAMILY = new Signature("0102030405"), OTHER = new Signature("0a0b0c0d0e");
    private Context ctx;
    private SharedPreferences prefs;

    @Before public void setup() {
        ctx = RuntimeEnvironment.getApplication();
        prefs = ctx.getSharedPreferences("atomix", Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        shadowOf(ctx.getPackageManager()).getInternalMutablePackageInfo(ctx.getPackageName()).signatures = new Signature[]{FAMILY};
    }

    @After public void reset() { Handover.takeBack(prefs); }

    private void installDexicon(Signature sig) {
        PackageInfo pi = new PackageInfo();
        pi.packageName = Handover.DEXICON;
        pi.signatures = new Signature[]{sig};
        shadowOf(ctx.getPackageManager()).installPackage(pi);
    }

    private HandoverActivity launch() {
        ActivityController<HandoverActivity> c = Robolectric.buildActivity(HandoverActivity.class);
        c.get().setTheme(R.style.Theme_Utxo);
        shadowOf(c.get()).setCallingPackage(Handover.DEXICON);
        c.create();
        return c.get();
    }

    @Test public void lookAlikeDexiconWithAnotherKeyIsRefused() {
        installDexicon(OTHER);
        Handover.setDone(prefs, 1L);
        HandoverActivity a = launch(); ShadowActivity sa = shadowOf(a);
        assertTrue(a.isFinishing());
        assertEquals(Activity.RESULT_CANCELED, sa.getResultCode());
        assertTrue(sa.getResultIntent().getStringExtra("reason").contains("not signed"));
    }

    @Test public void handedOverAndClearAnswersOkWithoutATap() {
        installDexicon(FAMILY);
        Handover.setDone(prefs, 1L);
        HandoverActivity a = launch(); ShadowActivity sa = shadowOf(a);
        assertTrue(a.isFinishing());
        assertEquals(Activity.RESULT_OK, sa.getResultCode());
        assertTrue(sa.getResultIntent().getBooleanExtra("handover", false));
        assertEquals("com.eurobuddha.atomix", sa.getResultIntent().getStringExtra("app"));
        assertEquals(BuildConfig.VERSION_CODE, sa.getResultIntent().getIntExtra("version", -1));
        assertTrue(sa.getResultIntent().getLongExtra("ts", 0) > 1L);
    }

    @Test public void handedOverButStillBlockedShowsTheScreen() {
        installDexicon(FAMILY);
        Handover.setDone(prefs, 1L);
        prefs.edit().putBoolean(Handover.P_OTC_PENDING, true).commit();   // OTC withdrawal not on the wire yet
        HandoverActivity a = launch(); ShadowActivity sa = shadowOf(a);
        assertFalse(a.isFinishing());
        assertEquals(Activity.RESULT_CANCELED, sa.getResultCode());
    }

    @Test public void notHandedOverShowsTheScreen() {
        installDexicon(FAMILY);
        HandoverActivity a = launch(); ShadowActivity sa = shadowOf(a);
        assertFalse(a.isFinishing());
        assertFalse(Handover.done(prefs));
    }
}
