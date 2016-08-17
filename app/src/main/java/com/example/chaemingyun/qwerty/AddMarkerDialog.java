package com.example.chaemingyun.qwerty;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * Created by 송원근 on 2016-08-10.
 * dialog 생성 class
 */

public class AddMarkerDialog extends Dialog implements View.OnClickListener {

    private EditText editTextTitle, editTextContents;
    private ImageView imageView;
    private Button addOk, addCancel;
    private String title, snippet;
    final int OPEN_GELLERY = 100;
    Activity ac;

    public AddMarkerDialog(Context context) {
        super(context);
        ac = (Activity) context;
    }

    ;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_addmarker);

        editTextTitle = (EditText) findViewById(R.id.title);
        editTextContents = (EditText) findViewById(R.id.contents);
        imageView = (ImageView) findViewById(R.id.gellery_image);
        addOk = (Button) findViewById(R.id.addOK);
        addCancel = (Button) findViewById(R.id.addCancel);

        addOk.setOnClickListener(this);
        addCancel.setOnClickListener(this);
        imageView.setOnClickListener(this);
    }

    public String getEditTextTitle() {
        return title;
    }

    public String getEditTextContents() {
        return snippet;
    }

    @Override
    public void onClick(View view) {
        if (view == addOk) {
            title = editTextTitle.getText().toString();
            snippet = editTextContents.getText().toString();
            dismiss();
        } else if (view == addCancel) {
            cancel();
        } else if (view == imageView) {
            //갤러리 불러오기
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
            ac.startActivityForResult(intent, OPEN_GELLERY);
        }
    }

    public void setImageViewImage(Bitmap bm) {
        imageView.setImageBitmap(bm);
    }

    //입력창 초기화
    public void clearText() {
        imageView.setImageResource(R.drawable.add_image);
        editTextContents.setText("");
        editTextTitle.setText("");
        title = null;
        snippet = null;
    }
}