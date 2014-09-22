package com.thunsaker.rapido.app;

import com.thunsaker.zapato.dagger.BaseFragment;

public class BaseRapidoFragment extends BaseFragment {
    @Override
    protected Object[] getActivityModules() {
        return new Object[] {
                new RapidoActivityModule(this.getActivity())
        };
    }
}
