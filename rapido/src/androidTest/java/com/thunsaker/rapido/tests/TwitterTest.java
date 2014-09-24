package com.thunsaker.rapido.tests;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class TwitterTest {
    @Test
    public void testHasTwitterKey() throws Exception {
        String twit_key = System.getenv("RAPIDO_TWIT_KEY");
        Assert.assertTrue(twit_key != null);
    }
}