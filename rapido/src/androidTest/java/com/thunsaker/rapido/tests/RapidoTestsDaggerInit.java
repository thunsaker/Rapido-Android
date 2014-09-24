package com.thunsaker.rapido.tests;

import com.thunsaker.android.common.test.TestsDaggerInit;
import com.thunsaker.rapido.tests.app.TestRapidoAppModule;

import dagger.ObjectGraph;

public class RapidoTestsDaggerInit implements TestsDaggerInit {
    @Override
    public ObjectGraph getObjectGraph() {
        ObjectGraph objectGraph = ObjectGraph.create(new TestRapidoAppModule());
        objectGraph.validate();
        return objectGraph;
    }
}