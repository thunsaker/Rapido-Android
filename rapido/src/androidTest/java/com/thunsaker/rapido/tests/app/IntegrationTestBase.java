package com.thunsaker.rapido.tests.app;

import com.thunsaker.rapido.tests.RapidoTestsDaggerInit;

import org.junit.BeforeClass;

import dagger.ObjectGraph;

public class IntegrationTestBase {
    protected static ObjectGraph objectGraph;

    @BeforeClass
    public static void beforeClass() {
        objectGraph = new RapidoTestsDaggerInit().getObjectGraph();
    }
}