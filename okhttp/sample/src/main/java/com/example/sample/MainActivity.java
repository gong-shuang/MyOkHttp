package com.example.sample;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.TraceCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.style.UpdateAppearance;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 1;

    public static  String stringBuf ;
    UtilOkHttp utilOkHttp;

    public static final int UPDATE_VIEW =1;
    public static final int UPDATE_IMAGE =2;

    private TextView textView ;
    private ImageView imageView ;
    public static Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView)findViewById(R.id.textView);
        imageView = (ImageView) findViewById(R.id.image_View);

        utilOkHttp = new  UtilOkHttp(handler);

        getRuntimePermisssion();

        Log.d(TAG, " start!");
        Log.d(TAG, " String:" + getApplication().getPackageName());
        Log.d(TAG, " String:" + getApplication());


        float total_memory =
                Runtime.getRuntime().totalMemory()*1.0f/1024/1024;

        Log.d(TAG, " memory:" + total_memory );

    }

    public void getRuntimePermisssion(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case UPDATE_VIEW :
                    textView.setText(stringBuf);
                    break;
                case UPDATE_IMAGE :
                    imageView.setImageBitmap(bitmap);
                    break;
                default:
                    break;
            }
        }
    };

    public void doGet(View view){
        Log.i(TAG,"Thread:" + Thread.currentThread().getName());
        try {
            utilOkHttp.doGet();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doPost(View view){
        try {
            utilOkHttp.doPost();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void doPostString(View view){
        utilOkHttp.doPostString();

    }

    public void doPostFile(View view){
        utilOkHttp.doPostFile();

    }

    public void doUpload(View view){
        utilOkHttp.doUpload();

    }

    public void doDownload(View view){
        utilOkHttp.doDownload();

    }

    public void doDownloadImage(View view){
        utilOkHttp.doDownloadImg();
    }
}
