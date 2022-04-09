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

    Button bt1;
    SketchpadView pad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt1 = findViewById(R.id.bt1);
        pad = findViewById(R.id.pad);

        SketchpadData sketchpadData = new SketchpadData();
        pad.setSketchData(sketchpadData);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.rect_area);
                Log.e("test", "" + bitmap.getWidth() + bitmap.getHeight());
                pad.addRectRecord(bitmap, 520, 520);
            }
        });

    }
}