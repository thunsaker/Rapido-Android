package com.thunsaker.rapido.app;

import android.os.Bundle;

import com.thunsaker.android.common.BaseActivity;

public class BaseRapidoActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Object[] getActivityModules() {
        return new Object[] {
                new RapidoActivityModule(this)
        };
    }
}