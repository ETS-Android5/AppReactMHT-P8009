package com.mht.myxdemo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mht.myxdemo.R;
import com.mht.myxdemo.manaer.PrintfManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PrincipalActivity extends Activity {

    private TextView tv_main_bluetooth;
    private Button btn_main_print_image;
    private ImageView pdfView;

    private PrintfManager printfManager;

    private Context context;

    private static final String FILENAME = "sample.pdf";
    private ParcelFileDescriptor mFileDescriptor;
    private PdfRenderer mPdfRenderer;
    private PdfRenderer.Page mCurrentPage;
    private int mPageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        initView();
        initData();
        setLister();



        /*try {
            openRenderer(this);
            showPage(mPageIndex);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error! " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }*/
    }

    private Bitmap decodeResource(Resources resources, int id) {
        TypedValue value = new TypedValue();
        resources.openRawResource(id, value);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inTargetDensity = value.density;
        return BitmapFactory.decodeResource(resources, id, opts);
    }

    private void openRenderer(Context context, File file) throws IOException {
        if (!file.exists()) {
            // Since PdfRenderer cannot handle the compressed asset file directly, we copy it into
            // the cache directory.
            InputStream asset = context.getAssets().open(FILENAME);
            FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            asset.close();
            output.close();
        }
        mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        // This is the PdfRenderer we use to render the PDF.
        if (mFileDescriptor != null) {
            mPdfRenderer = new PdfRenderer(mFileDescriptor);
        }
    }

    private void showPage(int index) {
        if (mPdfRenderer.getPageCount() <= index) {
            return;
        }
        // Make sure to close the current page before opening another one.
        if (null != mCurrentPage) {
            mCurrentPage.close();
        }
        // Use `openPage` to open a specific page in PDF.
        mCurrentPage = mPdfRenderer.openPage(index);
        // Important: the destination bitmap must be ARGB (not RGB).
        Bitmap bitmap = Bitmap.createBitmap(mCurrentPage.getWidth(), mCurrentPage.getHeight(),
                Bitmap.Config.ARGB_8888);
        // Here, we render the page onto the Bitmap.
        // To render a portion of the page, use the second and third parameter. Pass nulls to get
        // the default result.
        // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
        mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        // We are ready to show the Bitmap to user.
        pdfView.setImageBitmap(bitmap);
    }

    private Bitmap getPDFPageBitmap(int index) {
        Bitmap bitmap = null;
        if (mPdfRenderer.getPageCount() <= index) {
            return bitmap;
        }
        // Make sure to close the current page before opening another one.
        if (null != mCurrentPage) {
            mCurrentPage.close();
        }
        // Use `openPage` to open a specific page in PDF.
        mCurrentPage = mPdfRenderer.openPage(index);
        // Important: the destination bitmap must be ARGB (not RGB).
        bitmap = Bitmap.createBitmap(mCurrentPage.getWidth(), mCurrentPage.getHeight(),
                Bitmap.Config.ARGB_8888);
        // Here, we render the page onto the Bitmap.
        // To render a portion of the page, use the second and third parameter. Pass nulls to get
        // the default result.
        // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
        mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        // We are ready to show the Bitmap to user.
        pdfView.setImageBitmap(bitmap);

        return bitmap;

    }



    private void setLister() {

        btn_main_print_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Bitmap bitmap = decodeResource(getResources(), R.mipmap.oynn_image);
                //A4打印机的有效打印宽度是210mm，美印熊的有效打印宽度是48mm
                //自定义打印宽度其他打印宽度，比如176

                Bitmap bitmap = getPDFBitmap(); //getBitmapByView(pdfView);
                printfManager.printf(210 * 8, (210 * 8 * bitmap.getHeight()) / bitmap.getWidth(), bitmap, PrincipalActivity.this);
            }
        });

        printfManager.addBluetoothChangLister(new PrintfManager.BluetoothChangLister() {
            @Override
            public void chang(String name, String address) {
                tv_main_bluetooth.setText(name);
            }
        });

        tv_main_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrintfBlueListActivity.startActivity(PrincipalActivity.this);
            }
        });

    }

    private void initData() {
        printfManager = PrintfManager.getInstance(context);
        printfManager.defaultConnection();
    }

    private void initView() {
        btn_main_print_image = (Button) findViewById(R.id.btn_main_print_image);
        tv_main_bluetooth = (TextView) findViewById(R.id.tv_main_bluetooth);
        pdfView = (ImageView)findViewById(R.id.pdfview);
    }

    /**
     * 获取View的Bitmap
     *
     * @param view
     * @return
     */
    public Bitmap getBitmapByView(View view) {
        Bitmap bitmap = null;
        try {
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();  //启用DrawingCache并创建位图
            bitmap = Bitmap.createBitmap(view.getDrawingCache()); //创建一个DrawingCache的拷贝，因为DrawingCache得到的位图在禁用后会被回收
            view.setDrawingCacheEnabled(false);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return bitmap;
    }

    public Bitmap createViewBitmap(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }

    private Bitmap getPDFBitmap() {
        Bitmap bitmap = null;
        // In this sample, we read a PDF from the assets directory.
        File file = new File(context.getCacheDir(), FILENAME);

         try {
            openRenderer(this, file);
            bitmap = getPDFPageBitmap(mPageIndex);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error! " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return bitmap;
    }


}
