package com.lavanidad.qiushui.view;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.lavanidad.qiushui.R;


public class CommonDialog extends DialogFragment {

    private TextView tv_content;
    private Button bt_left, bt_right;

    private String content;
    private String leftBtnString, rightBtnString;
    private OnDialogChooseListener onDialogChooseListener;

    public CommonDialog(String content, String leftBtnString, String rightBtnString) {
        this.content = content;
        this.leftBtnString = leftBtnString;
        this.rightBtnString = rightBtnString;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);

        Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getDecorView().setBackgroundColor(Color.TRANSPARENT);
        window.setDimAmount(0.5f);

        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);

        View view = inflater.inflate(R.layout.dialog_common_layout, null);
        tv_content = view.findViewById(R.id.tv_content);
        bt_left = view.findViewById(R.id.bt_left);
        bt_right = view.findViewById(R.id.bt_right);

        if (!TextUtils.isEmpty(content)) {
            tv_content.setText(content);
        }
        if (!TextUtils.isEmpty(leftBtnString)) {
            bt_left.setText(leftBtnString);
        }
        if (!TextUtils.isEmpty(rightBtnString)) {
            bt_right.setText(rightBtnString);
        }

        bt_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDialogChooseListener != null) {
                    onDialogChooseListener.onRightBtnClick();
                }
                dismiss();
            }
        });

        bt_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDialogChooseListener != null) {
                    onDialogChooseListener.onLeftBtnClick();
                }
                dismiss();
            }
        });

        return view;
    }

    public void setOnDialogChooseListener(OnDialogChooseListener onDialogChooseListener) {
        this.onDialogChooseListener = onDialogChooseListener;
    }

    public interface OnDialogChooseListener {

        void onLeftBtnClick();

        void onRightBtnClick();
    }
}
