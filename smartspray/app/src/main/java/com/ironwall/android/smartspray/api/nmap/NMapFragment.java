package com.ironwall.android.smartspray.api.nmap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ironwall.android.smartspray.R;
import com.nhn.android.maps.NMapContext;

/**
 * Created by KimJS on 2016-07-24.
 */
public class NMapFragment extends NMapBaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nmap, container, false);
    }

    public NMapContext getNMapContext() {
        return super.getNMapContext();
    }
}
