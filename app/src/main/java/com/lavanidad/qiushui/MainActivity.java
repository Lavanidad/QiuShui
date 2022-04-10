package com.lavanidad.qiushui;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.lavanidad.qiushui.bean.SketchpadData;

public class MainActivity extends AppCompatActivity {

    Button bt_none, bt_rect, bt_add_rect;
    SketchpadView pad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt_none = findViewById(R.id.bt_none);
        bt_rect = findViewById(R.id.bt_rect);
        bt_add_rect = findViewById(R.id.bt_add_rect);
        pad = findViewById(R.id.pad);

        //init
        SketchpadData sketchpadData = new SketchpadData();
        pad.setSketchData(sketchpadData);

        bt_none.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bt_add_rect.setVisibility(View.INVISIBLE);
                pad.setDrawMode(SketchpadView.DrawMode.TYPE_NONE);
            }
        });

        bt_rect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bt_add_rect.setVisibility(View.VISIBLE);
                //step1
                pad.setDrawMode(SketchpadView.DrawMode.TYPE_RECT);
            }
        });

        bt_add_rect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.rect_area);
                //step2
                pad.addRectRecord(bitmap, 400, 400);
            }
        });

    }
}