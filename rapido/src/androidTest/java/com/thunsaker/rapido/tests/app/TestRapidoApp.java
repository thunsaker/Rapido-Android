package com.thunsaker.rapido.tests.app;

import android.content.Context;

import com.thunsaker.rapido.app.RapidoApp;

import java.util.List;

import dagger.ObjectGraph;

public class TestRapidoApp extends RapidoApp {
    public static TestRapidoApp from(Context context) {
        return (TestRapidoApp) context.getApplicationContext();
    }

    ObjectGraph objectGraph;

    @Override
    protected List<Object> getAppModules() {
        List<Object> modules = super.getAppModules();
        modules.add(new TestRapidoAppModule());
        return modules;
    }

//    public static <T> T injectMocks(T object) {
//        TestRapidoApp app = (TestRapidoApp) Robolectric.application;
//        return app.inject(object);
//    }
}
