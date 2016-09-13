package com.ironwall.android.smartspray.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ironwall.android.smartspray.R;
import com.ironwall.android.smartspray.service.RingtonePlayingService;

/**
 * Created by KimJS on 2016-09-09.
 */
public class PasswordFragment extends Fragment {

    private Context mContext;
    private Button btSubmit;
    private EditText etPassword;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContext = getActivity();

        View view = inflater.inflate(R.layout.fragment_password, container, false);
        //init layout
        etPassword = (EditText)view.findViewById(R.id.etPassword);
        btSubmit = (Button)view.findViewById(R.id.btSubmit);
        btSubmit.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = etPassword.getText().toString();

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
                String pw = sharedPref.getString("pref_my_password", "");

                if(input.equals(pw))
                {
                    Toast.makeText(mContext, "pw: " + pw, Toast.LENGTH_SHORT).show();

                    // 벨소리 서비스 종료
                    Intent intent = new Intent(mContext, RingtonePlayingService.class);
                    mContext.stopService(intent);

                    //fragment 전환
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    ResultFragment rf = new ResultFragment();
                    fragmentTransaction.replace(R.id.fragment_container, rf);
                    fragmentTransaction.commit();
                }

            }
        });


        return view;
    }
}
