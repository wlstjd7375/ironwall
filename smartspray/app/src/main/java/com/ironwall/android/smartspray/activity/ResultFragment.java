package com.ironwall.android.smartspray.activity;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ironwall.android.smartspray.R;

/**
 * Created by KimJS on 2016-09-10.
 */
public class ResultFragment extends Fragment {

    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContext = getActivity();
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        //TODO
        // 안전 문자 보낼지 확인후 전송

        return view;
    }
}
