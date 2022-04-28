package com.lavanidad.qiushui.adapter;


import android.widget.CheckBox;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lavanidad.qiushui.R;
import com.lavanidad.qiushui.map.bean.DrawRecord;


public class DrawAdapter extends BaseQuickAdapter<DrawRecord, BaseViewHolder> {

    public DrawAdapter() {
        super(R.layout.item_drawrect_content);
    }


    @Override
    protected void convert(BaseViewHolder helper, DrawRecord item) {
        ((CheckBox) helper.getView(R.id.cb_check)).setChecked(item.isChecked);

        helper.setText(R.id.tv_name, "" + item.name)
                .addOnClickListener(R.id.cb_check);

    }
}
