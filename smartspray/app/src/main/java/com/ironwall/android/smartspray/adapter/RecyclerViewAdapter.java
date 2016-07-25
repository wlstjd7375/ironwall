package com.ironwall.android.smartspray.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ironwall.android.smartspray.R;
import com.ironwall.android.smartspray.activity.SosActivity;
import com.ironwall.android.smartspray.database.DBManager;
import com.ironwall.android.smartspray.dto.SosNumber;

import java.util.ArrayList;

/**
 * Created by KimJS on 2016-07-24.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
private final TypedValue mTypedValue = new TypedValue();
private int mBackground;
private ArrayList<SosNumber> mDataset;
private Context mContext;
/*
* 여기에 처음설정
* */
class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;

    public final TextView tvSosName;
    public final TextView tvSosNumber;
    public final ImageView ivDelete;

    public ViewHolder(View view) {
        super(view);
        mView = view;
        tvSosName = (TextView) view.findViewById(R.id.tvSosName);
        tvSosNumber = (TextView) view.findViewById(R.id.tvSosNumber);
        ivDelete = (ImageView) view.findViewById(R.id.ivDelete);
    }
}


    public RecyclerViewAdapter(Context context, ArrayList<SosNumber> myDataset) {
        //context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        //this.mBackground = mTypedValue.resourceId;
        mDataset = myDataset;
        mContext = context;
    }

    /*
    * 너가 만든 각 리스트에 해당하는 뷰 정의
    * */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.number_list_view, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    /*
    * 여기에 각 뷰마다 해당하는 작업하면됨
    * 뷰에 아이템 뿌려준다던지 클릭했을떄 뭐할지 등등등
    * */
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.tvSosName.setText(mDataset.get(position).name);
        holder.tvSosNumber.setText(mDataset.get(position).number);

        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder= new AlertDialog.Builder(mContext); //AlertDialog.Builder 객체 생성
                builder.setMessage("Are you sure to delete this number?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        String number = holder.tvSosNumber.getText().toString().trim();
                        int result = DBManager.getManager(mContext).deleteSosNumber(number);
                        if(result == 0) { //fail
                            Toast.makeText(mContext, "Delete Fail!", Toast.LENGTH_SHORT).show();
                        }
                        ((SosActivity)mContext).updateCardView();
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
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
