package com.thunsaker.rapido.app;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.thunsaker.android.common.dagger.DaggerApplication;

import java.util.Collections;
import java.util.List;

public class RapidoApp extends DaggerApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        try {
            PackageManager manager = getPackageManager();
            ApplicationInfo applicationInfo = manager.getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = applicationInfo.metaData;
            // TODO: IF there is an issue with the key existing
//            String crashKey = bundle.getString("com.crashlytics.ApiKey");
//            if(crashKey == null || crashKey.length() == 0)
//            bundle.putString("com.crashlytics.ApiKey", System.getenv("RAPIDO_CRASH_KEY"));
            bundle.putString("com.facebook.sdk.ApplicationId", System.getenv("RAPIDO_FACE_ID"));
            bundle.putString("com.google.android.maps.v2.API_KEY", System.getenv("RAPIDO_MAPS_KEY"));
            applicationInfo.metaData = bundle;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected List<Object> getAppModules() {
        return Collections.<Object>singletonList(new RapidoAppModule());
    }
}
