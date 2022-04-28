package com.lavanidad.qiushui;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lavanidad.qiushui.adapter.DrawAdapter;
import com.lavanidad.qiushui.map.SketchpadView2;
import com.lavanidad.qiushui.map.bean.DrawRecord;
import com.lavanidad.qiushui.map.bean.SketchpadData2;
import com.lavanidad.qiushui.view.CommonDialog;


import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, SketchpadView2.OnDrawControlListener {

    private Button bt_dot, bt_line, bt_rect, bt_add_waterdot, bt_add_draindot, bt_delete_rect, bt_save_rect;

    private RecyclerView rv_rect;

    private RelativeLayout rl_child_menu;//子菜单区域

    private SketchpadView2 sketchpadView;//绘画地图

    private SketchpadData2 sketchpadData;
    private boolean dotChecked = false;
    private List<DrawRecord> rectRecordList;
    private DrawAdapter drawRectAdapter;
    private List<DrawRecord> rectRecordDeleteList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt_dot = findViewById(R.id.bt_dot);
        bt_line = findViewById(R.id.bt_line);
        bt_rect = findViewById(R.id.bt_rect);
        bt_add_waterdot = findViewById(R.id.bt_add_waterdot);
        bt_add_draindot = findViewById(R.id.bt_add_draindot);
        bt_delete_rect = findViewById(R.id.bt_delete_rect);
        bt_save_rect = findViewById(R.id.bt_save_rect);
        rl_child_menu = findViewById(R.id.rl_child_menu);
        sketchpadView = findViewById(R.id.sketchpadView);
        rv_rect = findViewById(R.id.rv_rect);


        initView();
        initListener();
        initTestData();
    }


    protected void initView() {
        sketchpadData = new SketchpadData2();
        sketchpadView.setSketchData(sketchpadData);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.map);
        sketchpadView.addBackgroundBitmap(bitmap, 600, 600);

        rv_rect.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        drawRectAdapter = new DrawAdapter();
        rv_rect.setAdapter(drawRectAdapter);
        View headRect = getLayoutInflater().inflate(R.layout.item_drawrect_header, null);
        drawRectAdapter.setHeaderView(headRect);

        rectRecordList = new ArrayList<>();
        rectRecordDeleteList = new ArrayList<>();
        drawRectAdapter.setNewData(rectRecordList);
    }


    protected void initListener() {
        bt_dot.setOnClickListener(this);
        bt_line.setOnClickListener(this);
        bt_rect.setOnClickListener(this);
        bt_add_waterdot.setOnClickListener(this);
        bt_add_draindot.setOnClickListener(this);
        sketchpadView.setOnDrawControlListener(this);
        bt_save_rect.setOnClickListener(this);
        bt_delete_rect.setOnClickListener(this);


        drawRectAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                CheckBox checkBox = view.findViewById(R.id.cb_check);
                checkBox.setOnCheckedChangeListener(null);
                if (checkBox.isChecked()) {
                    rectRecordList.get(position).isChecked = true;
                    rectRecordDeleteList.add(rectRecordList.get(position));
                    sketchpadView.selectDrawItems(rectRecordList.get(position));
                } else {
                    rectRecordList.get(position).isChecked = false;
                    rectRecordDeleteList.remove(rectRecordList.get(position));
                    sketchpadView.unSelectDrawItems(rectRecordList.get(position));
                }
            }
        });
    }

    private void initTestData() {

//        Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(), R.mipmap.rect_area);
//        sketchpadView.addDrawRecordByPoint(bitmap3, 434, 447, 548, 548,0);
//        sketchpadView.setActionMode(SketchpadView2.ACTION_DRAW);

//        Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(), R.mipmap.drain_dot);
//        sketchpadView.addDotRecordByPoint(bitmap3, 434, 447, 548, 548,3);
//        sketchpadView.setActionMode(SketchpadView2.ACTION_DRAW);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_dot:
                if (dotChecked) {
                    dotChecked = false;
                    rl_child_menu.setVisibility(View.GONE);
                    sketchpadView.setActionMode(SketchpadView2.ACTION_NONE);
                } else {
                    dotChecked = true;
                    rl_child_menu.setVisibility(View.VISIBLE);
                    sketchpadView.setActionMode(SketchpadView2.ACTION_DRAW);
                }
                break;
            case R.id.bt_line:

                rl_child_menu.setVisibility(View.GONE);
                bt_dot.setBackground(getDrawable(R.mipmap.icon_dot_unpressed));

                sketchpadView.setActionMode(SketchpadView2.ACTION_DRAW);
                Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.mipmap.line_area1);
                sketchpadView.addDrawRecord(bitmap1, 120, 80, 1);
                break;
            case R.id.bt_rect:

                rl_child_menu.setVisibility(View.GONE);
                bt_dot.setBackground(getDrawable(R.mipmap.icon_dot_unpressed));

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.rect_area);
                sketchpadView.addDrawRecord(bitmap, 120, 120, 0);
                sketchpadView.setActionMode(SketchpadView2.ACTION_DRAW);
                break;
            case R.id.bt_add_waterdot:
                Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_dot);
                sketchpadView.addDotRecord(bitmap2, 90, 80, 2);
                sketchpadView.setActionMode(SketchpadView2.ACTION_DRAW);
                break;
            case R.id.bt_add_draindot:
                Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_dot);
                sketchpadView.addDotRecord(bitmap3, 90, 80, 3);
                sketchpadView.setActionMode(SketchpadView2.ACTION_DRAW);
                break;
            case R.id.bt_delete_rect://删除禁行区
                if (rectRecordDeleteList.size() == 0) {
                    ToastUtils.showShort("暂未选中任何条目");
                    return;
                }
                CommonDialog deleteRectDialog = new CommonDialog("确定删除选中条目？", "取消", "确认");
                deleteRectDialog.showNow(getSupportFragmentManager(), "");
                deleteRectDialog.setOnDialogChooseListener(new CommonDialog.OnDialogChooseListener() {
                    @Override
                    public void onRightBtnClick() {
                        for (int i = 0; i < rectRecordDeleteList.size(); i++) {
                            sketchpadView.unSelectDrawItems(rectRecordDeleteList.get(i));
                            rectRecordList.remove(rectRecordDeleteList.get(i));
                            sketchpadData.drawRecordList.remove(rectRecordDeleteList.get(i));
                        }
                        sketchpadView.refreshView();
                        drawRectAdapter.notifyDataSetChanged();
                        rectRecordDeleteList.clear();
                    }

                    @Override
                    public void onLeftBtnClick() {
                    }
                });
                break;
        }
    }

    /*
     ************************** padview 回调 ***********************
     */
    @Override
    public void onDrawConfirm(DrawRecord record, float[] phyPoints) {
        //  record.canSelected = false;
        if (rectRecordList.size() == 0) {
            rectRecordList.add(record);
            drawRectAdapter.notifyDataSetChanged();
        }
        for (int i = 0; i < rectRecordList.size(); i++) {
            if (record.name.equals(rectRecordList.get(i).name)) {
                return;
            }
        }
        rectRecordList.add(record);
        drawRectAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDrawCancel(DrawRecord record) {
        if (rectRecordList.size() > 0) {
            for (int i = 0; i < rectRecordList.size(); i++) {
                if (record.name.equals(rectRecordList.get(i).name)) {
                    rectRecordDeleteList.add(rectRecordList.get(i));
                    rectRecordList.remove(record);
                }
            }
            drawRectAdapter.notifyDataSetChanged();
            rectRecordDeleteList.clear();
        }
    }
}