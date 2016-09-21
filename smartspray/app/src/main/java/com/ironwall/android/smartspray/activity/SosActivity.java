package com.ironwall.android.smartspray.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ironwall.android.smartspray.R;
import com.ironwall.android.smartspray.adapter.RecyclerViewAdapter;
import com.ironwall.android.smartspray.database.DBManager;
import com.ironwall.android.smartspray.dto.SosNumber;
import com.ironwall.android.smartspray.global.GlobalVariable;

import java.util.ArrayList;

public class SosActivity extends AppCompatActivity {

    private Context mContext;
    private final static String LOG_TAG = "SosActivity##";
    //CardView
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private TextView tvMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);
        //mContext = getApplicationContext();
        mContext = this;
        initActivity();
        initCardView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(GlobalVariable.IS_DEBUG_MODE) {
            Log.d(LOG_TAG, "onResume()");
        }
        updateCardView();
    }

    private void initCardView() {
        //CardView
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        //!CardView
    }

    public void updateCardView() {
        ArrayList<SosNumber> numberList = new ArrayList<SosNumber>();
        numberList = DBManager.getManager(mContext).getAllSosNumber();

        //CardView
        mAdapter = new RecyclerViewAdapter(mContext, numberList);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initActivity() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tvMessage = (TextView) findViewById(R.id.tvMessage);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String name = sharedPref.getString("pref_my_name", "");
        String msg = name + " 님이 위급상황에 처했습니다. \n위치: [링크]";
        tvMessage.setText(msg);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog();
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
    }

    private void createDialog() {
        LayoutInflater inflater=getLayoutInflater();
        final View dialogView= inflater.inflate(R.layout.dialog_add_number, null);
        AlertDialog.Builder builder= new AlertDialog.Builder(this); //AlertDialog.Builder 객체 생성
        //builder.setTitle("Add SOS Number"); //Dialog 제목
        //builder.setIcon(android.R.drawable.ic_menu_add); //제목옆의 아이콘 이미지(원하는 이미지 설정)
        builder.setView(dialogView); //위에서 inflater가 만든 dialogView 객체 세팅 (Customize)
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                EditText etName = (EditText)dialogView.findViewById(R.id.etName);
                EditText etNumber = (EditText)dialogView.findViewById(R.id.etNumber);

                String name = etName.getText().toString().trim();
                String number = etNumber.getText().toString().trim();
                if(name.equals("") || number.equals("")) {
                    Toast.makeText(mContext, "Name and number shouldn't be a blank.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //if name and number are not null
                SosNumber sn = new SosNumber();
                /*if(number.length() >= 10) {
                    number = changeNumberFormat(number);
                }*/
                sn.name = name;
                sn.number = number;

                long result = DBManager.getManager(mContext).setSosNumber(sn);
                if(result == -1) { //fail
                    Toast.makeText(mContext, "Fail(Number already exist)", Toast.LENGTH_SHORT).show();
                }
                else {
                    updateCardView();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });

        AlertDialog dialog = builder.create();
        //Dialog의 바깥쪽을 터치했을 때 Dialog를 없앨지 설정
        dialog.setCanceledOnTouchOutside(false);//없어지지 않도록 설정
        dialog.show();
    }

    private String changeNumberFormat(String number) {
        number = number.replaceAll("(^02.{0}|^01.{1}|[0-9]{3})([0-9]+)([0-9]{4})","$1-$2-$3");
        /*
        StringBuffer sb = new StringBuffer(number);
        sb.insert(7, "-");
        sb.insert(3, "-");*/

        return number;
    }


}
