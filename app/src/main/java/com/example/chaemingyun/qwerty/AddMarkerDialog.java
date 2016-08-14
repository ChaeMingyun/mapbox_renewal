package com.example.chaemingyun.qwerty;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chaemingyun.qwerty.mapbox.MapActivity;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by 송원근 on 2016-08-10.
 * dialog 생성 class
 */

public class AddMarkerDialog extends Dialog implements View.OnClickListener {

    private EditText title,contents;
    private ImageView imageView;
    private Button addOk, addCancel;
    private String _title, _contents;
    private  Context context;
    final int OPEN_GELLERY = 100;
    Activity ac;

    public AddMarkerDialog(Context context) {
        super(context);
        ac = (Activity) context;
    }
;
    protected void  onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_addmarker);

        title = (EditText) findViewById(R.id.title);
        contents = (EditText) findViewById(R.id.contents);
        imageView = (ImageView) findViewById(R.id.gellery_image);
        addOk = (Button) findViewById(R.id.addOK);
        addCancel = (Button) findViewById(R.id.addCancel);

        addOk.setOnClickListener(this);
        addCancel.setOnClickListener(this);
        imageView.setOnClickListener(this);
    }

    public String getTitle(){
        return _title;
    }

    public String getContents(){
        return _contents;
    }

    @Override
    public void onClick(View view) {
        if(view == addOk){
            _title = title.getText().toString();
            _contents = contents.getText().toString();
            dismiss();
        }
        else if(view == addCancel){
            cancel();
        }else if(view == imageView){
            //갤러리 불러오기
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
            ac.startActivityForResult(intent, OPEN_GELLERY);
        }
    }

    public void setImageView_img(Bitmap bm){
        imageView.setImageBitmap(bm);
    }

    //입력창 초기화
    public void clearText(){
        imageView.setImageResource(R.drawable.add_image);
        contents.setText("");
        title.setText("");
        _title = null;
        _contents = null;
    }
}