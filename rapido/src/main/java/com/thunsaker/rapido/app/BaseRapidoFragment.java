package com.thunsaker.rapido.app;

import com.thunsaker.android.common.BaseFragment;

public class BaseRapidoFragment extends BaseFragment {
    @Override
    protected Object[] getActivityModules() {
        return new Object[] {
                new RapidoActivityModule(this.getActivity())
        };
    }
}
