package com.mht.myxdemo;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.mht.myxdemo.activity.PrincipalActivity;
import com.mht.myxdemo.activity.PrintfBlueListActivity;
import com.mht.myxdemo.manaer.PrintfManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class NavigationModule  extends ReactContextBaseJavaModule {
    private static ReactApplicationContext reactContext;
    private Intent intent;
    private String TAG = "NavigationModule";

    private static final String FILENAME = "sample.pdf";
    private ParcelFileDescriptor mFileDescriptor;
    private PdfRenderer mPdfRenderer;
    private PdfRenderer.Page mCurrentPage;
    private int mPageIndex = 0;
    private PrintfManager printfManager;

    NavigationModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }

    @NonNull
    @Override
    public String getName() {
        return "NavigationModule";
    } //The name of the component when it is called in the RN code

    @ReactMethod
    public void launchPrincipalActivity(){
        ReactApplicationContext context = getReactApplicationContext();
        intent = new Intent(context, PrincipalActivity.class);

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            intent.setFlags((Intent.FLAG_ACTIVITY_NEW_TASK));
            context.startActivity(intent);
        }
    }

    @ReactMethod
    public void connect(){
        ReactApplicationContext context = getReactApplicationContext();
        intent = new Intent(context, PrintfBlueListActivity.class);

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            intent.setFlags((Intent.FLAG_ACTIVITY_NEW_TASK));
            context.startActivity(intent);
        }
    }

    @ReactMethod
    public void print(){
        ReactApplicationContext context = getReactApplicationContext();

        //Get PrintfManager instance
        printfManager = PrintfManager.getInstance(context);
        printfManager.defaultConnection();

        Bitmap bitmap = getPDFBitmap(context);
        printfManager.printf(210 * 8, (210 * 8 * bitmap.getHeight()) / bitmap.getWidth(), bitmap, context.getCurrentActivity());
    }

    private Bitmap getPDFBitmap(Context context) {
        Bitmap bitmap = null;
        // In this sample, we read a PDF from the assets directory.
        File file = new File(context.getCacheDir(), FILENAME);

        try {
            openRenderer(context, file);
            bitmap = getPDFPageBitmap(mPageIndex);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error! " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return bitmap;
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

        return bitmap;
    }
}