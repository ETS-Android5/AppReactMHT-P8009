package com.mht.myxdemo.manaer;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;

import com.mht.myxdemo.utils.Util;

public class SharedPreferencesManager {

    public static int getPaperTotal(Context context) {
        return getSharedPreferences(context).getInt("total", 0);
    }

    public static int getPaperAlready(Context context) {
        return getSharedPreferences(context).getInt("already", 0);
    }

    public static int getPaperSurplus(Context context) {
        return getSharedPreferences(context).getInt("surplus", 0);
    }

    public static int getLanguageId(Context context) {
        String able= context.getResources().getConfiguration().locale.getCountry();
        if(able.equals("CN")){
            return getSharedPreferences(context).getInt("languageId", Util.Language.zh);
        }
        if(able.equals("TW")||able.equals("HK")||able.equals("MO")){
            return getSharedPreferences(context).getInt("languageId", Util.Language.cht);
        }
        return getSharedPreferences(context).getInt("languageId", Util.Language.en);
    }

    public static void saveLanguageId(Context context, int id) {
        getSharedPreferences(context).edit().putInt("languageId", id).commit();
    }

    /**
     * 更新打印纸张与剩余纸张
     */
    public static void saveAlreadyAndSurplus(Context context, int printfNumber) {
        int paperAlready = getPaperAlready(context);
        int paperSurplus = getPaperSurplus(context);
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt("already", paperAlready + printfNumber);
        edit.putInt("surplus", paperSurplus - printfNumber);
        edit.commit();
    }

    /**
     * 更新总纸张
     *
     * @param context
     * @param paper
     */
    public static void savePaperTotal(Context context, int paper) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt("total", paper);
        edit.putInt("already", 0);
        edit.putInt("surplus", paper);
        edit.commit();
    }


    public static void saveLabelNameFile(Context context, String path) {
        SharedPreferences mht = getSharedPreferences(context);
        mht.edit().putString("labelName", path).commit();
    }

    public static String getLabelNameFile(Context context) {
        SharedPreferences mht = getSharedPreferences(context);
        return mht.getString("labelName", null);
    }

    public static void clearBluetoothName(Context context) {
        saveBluetoothName(context, null);
    }

    public static void saveBluetoothName(Context context, String name) {
        SharedPreferences mht = getSharedPreferences(context);
        mht.edit().putString("blueName", name).commit();
    }

    public static void updateBluetooth(Context context, BluetoothDevice device) {
        saveBluetoothName(context, device.getName());
        saveBluetoothAddress(context, device.getAddress());
    }

    public static String getBluetoothName(Context context) {
        SharedPreferences mht = getSharedPreferences(context);
        return mht.getString("blueName", null);
    }

    /**
     * 保持本次连接的蓝牙地址
     *
     * @param context
     */
    public static void saveBluetoothAddress(Context context, String address) {
        SharedPreferences blue = getSharedPreferences(context);
        blue.edit().putString("blueAddress", address).commit();
    }

    /**
     * 获取上次连接的蓝牙地址
     *
     * @param context
     * @return
     */
    public static String getBluetoothAddress(Context context) {
        SharedPreferences blue = getSharedPreferences(context);
        return blue.getString("blueAddress", null);
    }

    public static void saveFastPrintfType(Context context, int type) {
        SharedPreferences.Editor edit = getSharedPreferences(context).edit();
        edit.putInt("fast_printf_type", type).commit();
    }

    public static int getFastPrintfType(Context context) {
        return getSharedPreferences(context).getInt("fast_printf_type", PrintfManager.ORDINARY);
    }

    public static void saveFastPrintfNumber(Context context, int number) {
        SharedPreferences.Editor edit = getSharedPreferences(context).edit();
        edit.putInt("fast_printf_number", number).commit();
    }

    public static int getFastPrintfNumber(Context context) {
        return getSharedPreferences(context).getInt("fast_printf_number", 1);
    }

    public static String getFastPrintfKeys(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getString("fast_key", null);
    }

    public static void saveFastPrintfKeys(Context context, String keys) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        sharedPreferences.edit().putString("fast_key", keys).commit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("MHT", Context.MODE_PRIVATE);
    }

    public static boolean isBitmapCompress(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        boolean modify = sharedPreferences.getBoolean("is_compress", true);
        return modify;
    }

    public static void setBitmapCompress(Context context, boolean isCompress) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        sharedPreferences.edit().putBoolean("is_compress", isCompress).commit();
    }


}
