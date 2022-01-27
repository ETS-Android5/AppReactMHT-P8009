package com.mht.myxdemo.manaer;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mht.myxdemo.MyApplication;
import com.mht.myxdemo.R;
import com.mht.myxdemo.utils.Util;

import static android.view.Gravity.NO_GRAVITY;

public class PopupWindowManager {

    private PopupWindow popupWindow;
    private PopCallback popCallback;
    private View view;
    private TextView tv_pop_title;
    private TextView tv_pop_content;
    private TextView tv_pop_input_name;
    private EditText et_pop;
    private Button cancel;
    private Button determine;

    public void setPopCallback(PopCallback popCallback) {
        this.popCallback = popCallback;
    }

    Context context = null;

    private PopupWindowManager() {
        view = LayoutInflater.from(MyApplication.getInstance()).inflate(R.layout.pop_input, null, false);
        tv_pop_title = (TextView) view.findViewById(R.id.tv_pop_title);
        tv_pop_content = (TextView) view.findViewById(R.id.tv_pop_content);
        tv_pop_input_name = (TextView) view.findViewById(R.id.tv_pop_input_name);
        et_pop = (EditText) view.findViewById(R.id.et_pop);
        cancel = (Button) view.findViewById(R.id.cancel);
        determine = (Button) view.findViewById(R.id.determine);
    }

    static class PopupWindowManagerHolder {
        private static PopupWindowManager instance = new PopupWindowManager();
    }


    public static PopupWindowManager getInstance(Context context) {
        if (PopupWindowManagerHolder.instance.context == null) {
            PopupWindowManagerHolder.instance.context = context.getApplicationContext();
        }
        return PopupWindowManagerHolder.instance;
    }

    public void setEt_pop(String text){
        et_pop.setText("1");
    }

    public void showPopupWindow(String title, String content, String name, View v) {
        tv_pop_title.setText(title);
        tv_pop_content.setText(content);
        tv_pop_input_name.setText(name);
        et_pop.setText("");
        MyOnClickListener onClickListener = new MyOnClickListener();
        cancel.setOnClickListener(onClickListener);
        determine.setOnClickListener(onClickListener);
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.showAtLocation(v, NO_GRAVITY, 0, 0);
    }

    public interface PopCallback {
        void callBack(String data);
    }

    public void changOrdinaryInputType(){
        et_pop.setInputType(InputType.TYPE_CLASS_TEXT);
    }

    public void changIntegerInputType(){
        et_pop.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    public void changDecimalInputType(){
        et_pop.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.cancel:
                    if(popupWindow != null){
                        popupWindow.dismiss();
                        popupWindow = null;
                    }
                    break;
                case R.id.determine:
                    String text =et_pop.getText().toString().trim();
                    if(!text.equals("")){
                        try {
                            if (popCallback != null) {
                                popCallback.callBack(text);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                            Util.ToastText(context,context.getString(R.string.abnormal_data));
                        }
                        if(popupWindow != null){
                            popupWindow.dismiss();
                            popupWindow = null;
                        }
                    }else{
                        Util.ToastText(context,context.getString(R.string.please_improve_the_parameters));
                    }
                    break;
            }
            changDecimalInputType();
        }
    }
}
