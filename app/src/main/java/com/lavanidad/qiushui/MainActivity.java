package com.lavanidad.qiushui;


import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.lavanidad.qiushui.adapter.DrawAdapter;
import com.lavanidad.qiushui.map.SketchpadView2;
import com.lavanidad.qiushui.map.bean.DrawRecord;
import com.lavanidad.qiushui.map.bean.MapBean;
import com.lavanidad.qiushui.map.bean.SketchpadData2;
import com.lavanidad.qiushui.map.bean.YamlBean;
import com.lavanidad.qiushui.utils.ClickUtils;
import com.lavanidad.qiushui.utils.DrawUtil;
import com.lavanidad.qiushui.utils.ImageUtils;
import com.lavanidad.qiushui.view.CommonDialog;
import com.lavanidad.qiushui.widget.ZoomLayout;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, SketchpadView2.OnDrawControlListener {



    @BindView(R.id.bt_dot)
    Button bt_dot;
    @BindView(R.id.bt_line)
    Button bt_line;
    @BindView(R.id.bt_rect)
    Button bt_rect;
    @BindView(R.id.bt_eraser)
    Button bt_eraser;

    @BindView(R.id.rl_child_menu)
    RelativeLayout rl_child_menu;//子菜单区域

    @BindView(R.id.ll_dot_menu)
    LinearLayout ll_dot_menu;//点位子菜单
    @BindView(R.id.bt_add_waterdot)
    Button bt_add_waterdot;
    @BindView(R.id.bt_add_draindot)
    Button bt_add_draindot;

    @BindView(R.id.ll_eraser_area)
    LinearLayout ll_eraser_area;//橡皮擦操作区域
    @BindView(R.id.bt_eraser_select)
    Button bt_eraser_select;//选中，用于区分手势
    @BindView(R.id.bt_eraser_undo)
    Button bt_eraser_undo;
    @BindView(R.id.bt_eraser_clear)
    Button bt_eraser_clear;
    @BindView(R.id.bt_eraser_save)
    Button bt_eraser_save;

    @BindView(R.id.sketchpadView)
    SketchpadView2 sketchpadView;//绘画地图


    @BindView(R.id.ll_rectList)//drawlist
    LinearLayout ll_rectList;
    @BindView(R.id.rv_rect)
    RecyclerView rv_rect;
    @BindView(R.id.bt_delete_rect)
    Button bt_delete_rect;
    @BindView(R.id.bt_save_rect)
    Button bt_save_rect;
    @BindView(R.id.zoom_map)
    ZoomLayout zoom_map;
    @BindView(R.id.tv_title)
    TextView tv_title;

    private Context context;


    //地图
    private SketchpadData2 sketchpadData;

    //禁行区域
    private List<DrawRecord> rectRecordList;
    private DrawAdapter drawRectAdapter;
    private List<DrawRecord> rectRecordDeleteList;

    //TODO test
    private MapBean mapBean;
    private String base64 = "";
    private String json = "";
    private String yaml = "";
    private int width = 600;
    private int height = 600;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        context = this;

        base64 = getIntent().getStringExtra("base64");
        json = getIntent().getStringExtra("json");
        yaml = getIntent().getStringExtra("yaml");

        if (!TextUtils.isEmpty(yaml)) {
            Gson gson = new Gson();
            YamlBean yamlBean = gson.fromJson(yaml, YamlBean.class);
            width = yamlBean.getWidth();
            height = yamlBean.getHeight();
            tv_title.setText("" + yamlBean.getName());
        }

        initSketchpadView();
        initRecyclerView();
        initListener();
        rectRecordList = new ArrayList<>();
        rectRecordList.addAll(sketchpadData.drawRecordList);
        rectRecordDeleteList = new ArrayList<>();
        drawRectAdapter.setNewData(rectRecordList);

        mapBean = new MapBean();

    }


    protected void initListener() {
        bt_dot.setOnClickListener(this);
        bt_line.setOnClickListener(this);
        bt_rect.setOnClickListener(this);
        bt_eraser.setOnClickListener(this);

        bt_add_waterdot.setOnClickListener(this);
        bt_add_draindot.setOnClickListener(this);

        bt_eraser_select.setOnClickListener(this);
        bt_eraser_undo.setOnClickListener(this);
        bt_eraser_clear.setOnClickListener(this);
        bt_eraser_save.setOnClickListener(this);

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

        //json = "";
        //json = "{\"area\":[{\"desc\":\"描述\",\"id\":0,\"name\":\"禁行区0\",\"position\":[-15.0563,40.65188333333333,-9.606300000000001,40.65188333333333,-9.606300000000001,33.40188333333333,-15.0563,33.40188333333333],\"positionOrigin\":[54,349,174,469],\"rotation\":65.23767}],\"wall\":[{\"desc\":\"描述\",\"id\":1,\"name\":\"虚拟墙1\",\"position\":[-10.6963,45.424800000000005,-5.2463000000000015,45.424800000000005],\"positionOrigin\":[150,230,270,310],\"rotation\":38.584045}]}";

        Bitmap bitmap0 = BitmapFactory.decodeResource(getResources(), R.mipmap.rect_area);
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.mipmap.line_area);
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_water);
        Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_drain);
        if (!TextUtils.isEmpty(json)) {
            Gson gson = new Gson();
            MapBean bean = gson.fromJson(json, MapBean.class);
            Log.e("test", "json!=null:" + bean.toString());
            //
            if (bean.getPoi() != null) {
                for (int i = 0; i < bean.getPoi().size(); i++) {
                    int[] a = bean.getPoi().get(i).getPositionOrigin();
                    String name = bean.getPoi().get(i).getName();
                    if (name.contains("排水")) {
                        sketchpadView.addDotRecordByPoint(bitmap2, a[0], a[1], a[2], a[3], 3, name);
                    } else if (name.contains("补水")) {
                        sketchpadView.addDotRecordByPoint(bitmap3, a[0], a[1], a[2], a[3], 2, name);
                    }
                    sketchpadView.setActionMode(SketchpadView2.ACTION_DRAW);
                }
            }

            //
            if (bean.getArea() != null) {
                for (int i = 0; i < bean.getArea().size(); i++) {
                    int[] b = bean.getArea().get(i).getPositionOrigin();
                    String name = bean.getArea().get(i).getName();
                    sketchpadView.addDrawRecordByPoint(bitmap0, b[0], b[1], b[2], b[3], 0, bean.getArea().get(i).getRotation(), name);
                    sketchpadView.setActionMode(SketchpadView2.ACTION_DRAW);
                }
            }

            //
            if (bean.getWall() != null) {
                for (int i = 0; i < bean.getWall().size(); i++) {
                    int[] c = bean.getWall().get(i).getPositionOrigin();
                    String name = bean.getWall().get(i).getName();
                    sketchpadView.addDrawRecordByPoint(bitmap1, c[0], c[1], c[2], c[3], 1, bean.getWall().get(i).getRotation(), name);
                    sketchpadView.setActionMode(SketchpadView2.ACTION_DRAW);
                }
            }
        }
    }

    /**
     * 右侧列表
     */
    private void initRecyclerView() {
        rv_rect.setLayoutManager(new LinearLayoutManager(context));
        drawRectAdapter = new DrawAdapter();
        rv_rect.setAdapter(drawRectAdapter);
        View headRect = getLayoutInflater().inflate(R.layout.item_drawrect_header, null);
        drawRectAdapter.setHeaderView(headRect);
    }

    private void initSketchpadView() {
        sketchpadData = new SketchpadData2();
        sketchpadView.setSketchData(sketchpadData);
        if (!TextUtils.isEmpty(base64)) {
            Bitmap bitmap = ImageUtils.stringToBitmap(base64);
            sketchpadView.addBackgroundBitmap(bitmap, width, height);
        } else {
            ToastUtils.showShort("地图数据为空,显示默认图");
            sketchpadView.addBackgroundBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.map), width, height);
        }
        zoom_map.setVisibility(View.VISIBLE);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_dot:
                if (ClickUtils.isFastDoubleClick()) {
                    //短时间点击多次
                    return;
                }
                zoom_map.setVisibility(View.VISIBLE);
                ll_eraser_area.setVisibility(View.GONE);

                bt_line.setBackground(getDrawable(R.mipmap.icon_wall_unpressed));
                bt_eraser.setBackground(getDrawable(R.mipmap.icon_eraser_unpressed));
                bt_rect.setBackground(getDrawable(R.mipmap.icon_rect_unpressed));
                bt_dot.setBackground(getDrawable(R.mipmap.icon_dot_pressed));
                //子菜单
                rl_child_menu.setVisibility(View.VISIBLE);
                //子菜单二级
                ll_dot_menu.setVisibility(View.VISIBLE);
                ll_eraser_area.setVisibility(View.GONE);
                //地图
                sketchpadView.setVisibility(View.VISIBLE);

                //右侧
                ll_rectList.setVisibility(View.VISIBLE);
                sketchpadView.setActionMode(SketchpadView2.ACTION_DRAW);
                break;
            case R.id.bt_line:
                if (ClickUtils.isFastDoubleClick()) {
                    //短时间点击多次
                    return;
                }
                //地图
                zoom_map.setVisibility(View.VISIBLE);
                bt_line.setBackground(getDrawable(R.mipmap.icon_wall_pressed));
                bt_eraser.setBackground(getDrawable(R.mipmap.icon_eraser_unpressed));
                bt_rect.setBackground(getDrawable(R.mipmap.icon_rect_unpressed));
                bt_dot.setBackground(getDrawable(R.mipmap.icon_dot_unpressed));
                sketchpadView.setVisibility(View.VISIBLE);

                rl_child_menu.setVisibility(View.GONE);

                //右侧
                ll_rectList.setVisibility(View.VISIBLE);
                ll_eraser_area.setVisibility(View.GONE);
                sketchpadView.setActionMode(SketchpadView2.ACTION_DRAW);
                Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.mipmap.line_area);
                sketchpadView.addDrawRecord(bitmap1, 120, 80, 1);
                break;
            case R.id.bt_rect:
                if (ClickUtils.isFastDoubleClick()) {
                    //短时间点击多次
                    return;
                }
                //地图
                bt_eraser.setBackground(getDrawable(R.mipmap.icon_eraser_unpressed));
                bt_line.setBackground(getDrawable(R.mipmap.icon_wall_unpressed));
                bt_rect.setBackground(getDrawable(R.mipmap.icon_rect_pressed));
                bt_dot.setBackground(getDrawable(R.mipmap.icon_dot_unpressed));
                zoom_map.setVisibility(View.VISIBLE);
                sketchpadView.setVisibility(View.VISIBLE);

                rl_child_menu.setVisibility(View.GONE);

                //右侧
                ll_rectList.setVisibility(View.VISIBLE);
                ll_eraser_area.setVisibility(View.GONE);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.rect_area);
                sketchpadView.addDrawRecord(bitmap, 120, 120, 0);
                sketchpadView.setActionMode(SketchpadView2.ACTION_DRAW);
                break;
            case R.id.bt_add_waterdot:
                if (ClickUtils.isFastDoubleClick()) {
                    //短时间点击多次
                    return;
                }
                Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_water);
                sketchpadView.addDotRecord(bitmap2, 56, 74, 2);
                sketchpadView.setActionMode(SketchpadView2.ACTION_DRAW);
                break;
            case R.id.bt_add_draindot:
                if (ClickUtils.isFastDoubleClick()) {
                    //短时间点击多次
                    return;
                }
                Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_drain);
                sketchpadView.addDotRecord(bitmap3, 56, 74, 3);
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
            case R.id.bt_save_rect:
                break;
        }
    }

    private String getListToJSON() {
        MapBean mapBean = new MapBean();
        List<MapBean.PoiDTO> poiList = new ArrayList<>();
        List<MapBean.AreaDTO> areaList = new ArrayList<>();
        List<MapBean.WallDTO> wallList = new ArrayList<>();
        MapBean.PoiDTO poiDTO;
        MapBean.AreaDTO areaDTO;
        MapBean.WallDTO wallDTO;

        for (int i = 0; i < rectRecordList.size(); i++) {
            switch (rectRecordList.get(i).type) {
                case 0://矩形
                    float[] pointsRect = sketchpadView.getPhysicsPoints(DrawUtil.calculateRectResult(rectRecordList.get(i)));
                    int[] pointsRectOrigin = sketchpadView.getPhysicsPoints2(DrawUtil.calculateRectOrigin(rectRecordList.get(i)));
                    areaDTO = new MapBean.AreaDTO();
                    areaDTO.setId(i);
                    areaDTO.setName("禁行区" + i);
                    areaDTO.setDesc("描述");
                    areaDTO.setRotation(rectRecordList.get(i).mRotation);
                    areaDTO.setPosition(pointsRect);
                    areaDTO.setPositionOrigin(pointsRectOrigin);
                    areaList.add(areaDTO);
                    mapBean.setArea(areaList);
                    break;
                case 1://线
                    float[] pointsLine = sketchpadView.getPhysicsPoints(DrawUtil.calculateLineResult(rectRecordList.get(i)));
                    int[] pointsLineOrigin = sketchpadView.getPhysicsPoints2(DrawUtil.calculateRectOrigin(rectRecordList.get(i)));
                    wallDTO = new MapBean.WallDTO();
                    wallDTO.setId(i);
                    wallDTO.setName("虚拟墙" + i);
                    wallDTO.setDesc("描述");
                    wallDTO.setRotation(rectRecordList.get(i).mRotation);
                    wallDTO.setPosition(pointsLine);
                    wallDTO.setPositionOrigin(pointsLineOrigin);
                    wallList.add(wallDTO);
                    mapBean.setWall(wallList);
                    break;
                case 2:
                case 3:
                    float[] pointsDot = sketchpadView.getPhysicsPoints(DrawUtil.calculateDotResult(rectRecordList.get(i)));
                    int[] pointsDotOrigin = sketchpadView.getPhysicsPoints2(DrawUtil.calculateDotOrigin(rectRecordList.get(i)));
                    poiDTO = new MapBean.PoiDTO();
                    poiDTO.setId(i);
                    poiDTO.setName("兴趣点" + i);
                    poiDTO.setDesc("描述");
                    poiDTO.setPosition(pointsDot);
                    poiDTO.setPositionOrigin(pointsDotOrigin);
                    poiList.add(poiDTO);
                    mapBean.setPoi(poiList);
                    break;
            }
        }

        Gson gson = new Gson();
        Log.e("test", "发送json" + gson.toJson(mapBean));
        return gson.toJson(mapBean);
    }

    /*
     ************************** padview 回调 ***********************
     */
    @Override
    public void onDrawConfirm(DrawRecord record, float[] phyPoints) {
        // Log.e(TAG, "rect confirm:" + Arrays.toString(phyPoints));
        Log.e("rect", "" + record.mRotation);
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