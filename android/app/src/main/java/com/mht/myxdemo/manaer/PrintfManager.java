package com.mht.myxdemo.manaer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.print.sdk.PrinterConstants;
import com.android.print.sdk.PrinterInstance;
import com.mht.myxdemo.MyApplication;
import com.mht.myxdemo.R;
import com.mht.myxdemo.activity.PrintfBlueListActivity;
import com.mht.myxdemo.utils.Util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PrintfManager {

    public static int ORDINARY = 1, SERIALIZE = 2;

    protected String TAG = "PrintfManager";

    protected List<BluetoothChangLister> bluetoothChangListerList = new ArrayList<>();

    private ConnectSuccess connectSuccess;

    public void setConnectSuccess(ConnectSuccess connectSuccess) {
        this.connectSuccess = connectSuccess;
    }

    /**
     * 是否正在连接
     */
    private volatile boolean CONNECTING = false;

    public boolean isCONNECTING() {
        return CONNECTING;
    }

    /**
     * 添加蓝牙改变监听
     *
     * @param bluetoothChangLister
     */
    public void addBluetoothChangLister(BluetoothChangLister bluetoothChangLister) {
        bluetoothChangListerList.add(bluetoothChangLister);
    }

    /**
     * 解除观察者
     *
     * @param bluetoothChangLister
     */
    public void removeBluetoothChangLister(BluetoothChangLister bluetoothChangLister) {
        if (bluetoothChangLister == null) {
            return;
        }
        if (bluetoothChangListerList.contains(bluetoothChangLister)) {
            bluetoothChangListerList.remove(bluetoothChangLister);
        }
    }

    protected Context context;

    protected PrinterInstance mPrinter;


    private PrintfManager() {
    }

    static class PrintfManagerHolder {
        private static PrintfManager instance = new PrintfManager();
    }


    public static PrintfManager getInstance(Context context) {
        if (PrintfManagerHolder.instance.context == null) {
            PrintfManagerHolder.instance.context = context.getApplicationContext();
        }
        return PrintfManagerHolder.instance;
    }

    public void setPrinter(PrinterInstance mPrinter) {
        this.mPrinter = mPrinter;
    }

    public void connection() {
        if (mPrinter != null) {
            CONNECTING = true;
            mPrinter.openConnection();
        }
    }

    public PrinterInstance getPrinter() {
        return mPrinter;
    }

    private boolean isHasPrinter = false;

    public boolean isConnect() {
        return isHasPrinter;
    }

    public void disConnect(final String text) {
        MyApplication.getInstance().getCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                isHasPrinter = false;
                if (mPrinter != null) {
                    mPrinter.closeConnection();
                    mPrinter = null;
                }
                Util.ToastTextThread(context, text);
            }
        });
    }

    public void changBlueName(final String name) {
        MyApplication.getInstance().getCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Util.ToastTextThread(context, context.getString(R.string.chang_bluetooth_name_now));
                    String AT = "$OpenFscAtEngine$";
                    mPrinter.sendByteData(AT.getBytes());
                    Thread.sleep(500);
                    byte[] read = mPrinter.read();
                    if (read == null) {
                        Util.ToastTextThread(context, context.getString(R.string.chang_bluetooth_name_fail));
                    } else {
                        String readString = new String(read);
                        if (readString.contains("$OK,Opened$")) {//进入空中模式
                            mPrinter.sendByteData(("AT+NAME=" + name + "\r\n").getBytes());
                            Thread.sleep(500);
                            byte[] isSuccess = mPrinter.read();
                            if (new String(isSuccess).contains("OK")) {
                                Util.ToastTextThread(context, context.getString(R.string.chang_bluetooth_name_success));
                                SharedPreferencesManager.saveBluetoothName(context, name);
                            } else {
                                Util.ToastTextThread(context, context.getString(R.string.chang_bluetooth_name_fail));
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public final static int NAME_CHANG = 104;

    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String bluetoothName = context.getString(R.string.no_connect_blue_tooth);
            String bluetoothAddress = bluetoothName;
            switch (msg.what) {
                case PrinterConstants.Connect.SUCCESS://成功
                    isHasPrinter = true;
                    //Util.ToastText(context, context.getString(R.string.connection_success));
                    Util.ToastText(context, "connection success");
                    bluetoothName = SharedPreferencesManager.getBluetoothName(context);
                    bluetoothAddress = SharedPreferencesManager.getBluetoothAddress(context);
                    if (connectSuccess != null) {
                        connectSuccess.success();
                    }
                    break;
                case PrinterConstants.Connect.FAILED://失败
                    disConnect(context.getString(R.string.connection_fail));
                    break;
                case PrinterConstants.Connect.CLOSED://关闭
                    //disConnect(context.getString(R.string.bluetooth_disconnect));
                    disConnect("disconnected");
                    break;
                case NAME_CHANG://名称改变
                    BluetoothDevice device = (BluetoothDevice) msg.obj;
                    bluetoothAddress = device.getAddress();
                    bluetoothName = device.getName();
                    break;

            }

            for (BluetoothChangLister bluetoothChangLister : bluetoothChangListerList) {
                if (bluetoothChangLister != null) {
                    bluetoothChangLister.chang(bluetoothName, bluetoothAddress);
                }
            }
            CONNECTING = false;
        }
    };

    public void printf(final int width, final int height, final Bitmap bitmap, final Activity activity) {
        MyApplication.getInstance().getCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                if (isConnect()) {
                    //Util.ToastTextThread(activity, context.getString(R.string.print_now));
                    Util.ToastTextThread(activity, "printing");
                    realPrintfBitmap(width, height, bitmap);
                } else {
                    //Util.ToastTextThread(activity, context.getString(R.string.please_connect_bluetooth));
                    Util.ToastTextThread(activity, "please connect BLE");
                    PrintfBlueListActivity.startActivity(activity);
                }
            }
        });

    }

    public void printText(String content) {
        String charsetName = "gbk";
        byte[] data = null;

        try {
            if (charsetName != "") {
                data = content.getBytes(charsetName);
            } else {
                data = content.getBytes();
            }
        } catch (UnsupportedEncodingException var4) {
            var4.printStackTrace();
        }

        mPrinter.sendByteData(data);

    }

    public void defaultConnection() {
        String bluetoothName = SharedPreferencesManager.getBluetoothName(context);
        if (bluetoothName == null) {
            return;
        }

        String bluetoothAddress = SharedPreferencesManager.getBluetoothAddress(context);
        if (bluetoothAddress == null) {
            return;
        }

        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> bondedDevices = defaultAdapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            if (device.getAddress().equals(bluetoothAddress)) {
                mPrinter = new PrinterInstance(context, device, mHandler);
                connection();
                return;
            }
        }
    }

    /**
     * Connect
     *
     * @param mDevice
     */
    public void openPrinter(final BluetoothDevice mDevice) {
        MyApplication.getInstance().getCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                setPrinter(new PrinterInstance(context, mDevice, mHandler));
                // default is gbk...
                connection();
                //Connection Save Address + Name
                SharedPreferencesManager.updateBluetooth(context, mDevice);
            }
        });
    }

    /**
     * real printf
     *
     * @param width
     * @param height
     */
    private boolean realPrintfBitmap(int width, int height, Bitmap bitmap) {
        try {
            Bitmap newBitmap = resetBitmapSize(width, height, bitmap);
            newBitmap = convertGreyImgByFloyd(newBitmap, 128);
            byte[] bytes = bitmap2PrinterBytes(newBitmap, 0);

            byte[] stepBytes = {0x10, 0x21, 7};

            mPrinter.sendByteData(stepBytes);

            mPrinter.sendByteData(bytes);

            mPrinter.sendByteData("\n\n\n".getBytes());
            return true;
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            Util.ToastTextThread(context, context.getString(R.string.printf_error_check));
            return false;
        }
    }


    //Resize image
    public Bitmap resetBitmapSize(float newWidth, float newHeight, Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        //Calculate scaling
        float scaleWidth = newWidth / width;
        float scaleHeight = newHeight / height;
        //Get the matrix parameters you want to scale
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        //Get a new picture
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return bitmap;
    }

    public static Bitmap scaleMatrix(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float scaleW = width / w;
        float scaleH = height / h;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleW, scaleH); // 长和宽放大缩小的比例
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }

    private static Bitmap convertGreyImgByFloyd(Bitmap img, int threshold) {

        int width = img.getWidth();//获取位图的宽  
        int height = img.getHeight();//获取位图的高  
        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组  
        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] gray = new int[height * width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);
                gray[width * i + j] = (int) (0.29900 * red + 0.58700 * green + 0.11400 * blue); // 灰度转化公式;
            }
        }
        int e;
        //蛇形解析
        for (int i = 0; i < height; i++) {
            if (i % 2 == 1) {//从尾巴到头
                for (int j = width - 1; j >= 0; j--) {
                    int g = gray[width * i + j];
                    if (g > threshold) {
                        pixels[width * i + j] = 0xffffffff;
                        e = g - 255;
                    } else {
                        pixels[width * i + j] = 0xff000000;
                        e = g;
                    }
                    if (j < width - 1 && i < height - 1) {
                        //右边像素处理
                        gray[width * i + j + 1] += 3 * e / 8;
                        //下
                        gray[width * (i + 1) + j] += 3 * e / 8;
                        //右下
                        gray[width * (i + 1) + j + 1] += e / 4;
                    } else if (j == width - 1 && i < height - 1) {//靠右或靠下边的像素的情况
                        //下方像素处理
                        gray[width * (i + 1) + j] += 3 * e / 8;
                    } else if (j < width - 1 && i == height - 1) {
                        //右边像素处理
                        gray[width * (i) + j + 1] += 3 * e / 8;
                    }
                }
            } else {//从头到尾巴
                for (int j = 0; j < width; j++) {
                    int g = gray[width * i + j];
                    if (g > threshold) {
                        pixels[width * i + j] = 0xffffffff;
                        e = g - 255;
                    } else {
                        pixels[width * i + j] = 0xff000000;
                        e = g;
                    }
                    if (j < width - 1 && i < height - 1) {
                        //右边像素处理
                        gray[width * i + j + 1] += 3 * e / 8;
                        //下
                        gray[width * (i + 1) + j] += 3 * e / 8;
                        //右下
                        gray[width * (i + 1) + j + 1] += e / 4;
                    } else if (j == width - 1 && i < height - 1) {//靠右或靠下边的像素的情况
                        //下方像素处理
                        gray[width * (i + 1) + j] += 3 * e / 8;
                    } else if (j < width - 1 && i == height - 1) {
                        //右边像素处理
                        gray[width * (i) + j + 1] += 3 * e / 8;
                    }
                }
            }
        }
        Bitmap mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        mBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return mBitmap;
    }

    public static byte[] bitmap2PrinterBytes(Bitmap bitmap, int left) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        byte[] imgbuf = new byte[(width / 8 + left + 4) * height];
        byte[] bitbuf = new byte[width / 8];
        int[] p = new int[8];
        int s = 0;
        for (int y = 0; y < height; ++y) {
            int n;
            for (n = 0; n < width / 8; ++n) {
                int value;
                for (value = 0; value < 8; ++value) {
                    int grey = bitmap.getPixel(n * 8 + value, y);
                    //Log.e("grey", String.valueOf(grey));
                    int red = ((grey & 0x00FF0000) >> 16);
                    int green = ((grey & 0x0000FF00) >> 8);
                    int blue = (grey & 0x000000FF);
                    int gray = (int) (0.29900 * red + 0.58700 * green + 0.11400 * blue); // 灰度转化公式
                    //int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                    if (gray <= 128) {
                        gray = 1;//黑色
                    } else {
                        gray = 0;//白色
                    }
                    p[value] = gray;
                }
                value = p[0] * 128 + p[1] * 64 + p[2] * 32 + p[3] * 16 + p[4] * 8 + p[5] * 4 + p[6] * 2 + p[7];
                bitbuf[n] = (byte) value;
            }
            if (y != 0) {
                ++s;
                imgbuf[s] = 22;
            } else {
                imgbuf[s] = 22;
            }
            ++s;
            imgbuf[s] = (byte) (width / 8 + left);
            for (n = 0; n < left; ++n) {
                ++s;
                imgbuf[s] = 0;
            }
            for (n = 0; n < width / 8; ++n) {
                ++s;
                imgbuf[s] = bitbuf[n];
            }
            ++s;
            imgbuf[s] = 21;
            ++s;
            imgbuf[s] = 1;
        }
        return imgbuf;
    }

    public interface BluetoothChangLister {
        void chang(String name, String address);
    }

    public interface ConnectSuccess {
        void success();
    }
}
