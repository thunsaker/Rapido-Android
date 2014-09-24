package com.thunsaker.rapido.tests;

import android.app.Activity;

import com.thunsaker.rapido.ui.MainActivity;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(emulateSdk = 18, manifest = "src/main/AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class RapidoActivityRobolectricTest {
    @Test
    public void testActivityCreation() throws Exception {
        Activity activity = Robolectric.buildActivity(MainActivity.class).create().get();
        Assert.assertTrue(activity != null);
    }

    @Test
    public void testTheTruth() {
        Assert.assertTrue(true);
    }
}