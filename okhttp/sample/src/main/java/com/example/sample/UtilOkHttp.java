package com.example.sample;

import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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

import static com.example.sample.MainActivity.UPDATE_IMAGE;
import static com.example.sample.MainActivity.UPDATE_VIEW;

public class UtilOkHttp {
    private static final String TAG = "MainActivity";
    private String URL = "http://192.168.0.107:8080/imoocOkhttp/";
    private Handler handler;
    OkHttpClient okHttpClient = new OkHttpClient();

    public UtilOkHttp(Handler handler){
        this.handler = handler;
    }


    public void doPostFile(){
        // 1.获取OkHttpClient的对象
//        OkHttpClient okHttpClient = new OkHttpClient();

        File file = new File(Environment.getExternalStorageDirectory(),"imooc.jpg");
        if(!file.exists()){
            Log.i(TAG, " no exist!");
            return;
        }

        // mime type
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"),file);
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(URL + "postFile").post(requestBody).build();

        // 3.获取Call对象
        Call call = okHttpClient.newCall(request);

        // 4.执行call
        // 同步阻塞执行
        //      Response response = call.execute();
        // 异步执行
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                MainActivity.stringBuf = response.body().string();
                Log.d(TAG, MainActivity.stringBuf);
                Message message = new Message();
                message.what = UPDATE_VIEW;
                handler.sendMessage(message);
            }
        });
    }

    public void doUpload(){
        // 1.获取OkHttpClient的对象
//        OkHttpClient okHttpClient = new OkHttpClient();

        File file = new File(Environment.getExternalStorageDirectory(),"imooc.jpg");
        if(!file.exists()){
            Log.i(TAG, " no exist!");
            return;
        }

        MultipartBody.Builder multiBuilder=new MultipartBody.Builder();
        RequestBody multiBody = multiBuilder.setType(MultipartBody.FORM)
                .addFormDataPart("name","3333")
                .addFormDataPart("password", "555")
                .addFormDataPart("mPhoto", " xxx.jpg", RequestBody.create(MediaType.parse("application/octet-stream"),file))
                .build();

        // mime type
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(URL + "uploadInfo").post(multiBody).build();

        // 3.获取Call对象
        Call call = okHttpClient.newCall(request);

        // 4.执行call
        // 同步阻塞执行
        //      Response response = call.execute();
        // 异步执行
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                MainActivity.stringBuf = response.body().string();
                Log.d(TAG, MainActivity.stringBuf);
                Message message = new Message();
                message.what = UPDATE_VIEW;
                handler.sendMessage(message);
            }
        });
    }

    // 和 get 比较类似
    public void doDownload(){
        // 1.获取OkHttpClient的对象
//        OkHttpClient okHttpClient = new OkHttpClient();

        // 2.构造发送包
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .get()
                .url(URL + "files/xxx.jpg")   //这个文件名和上传的文件名一样
                .build();

        // 3.获取Call对象
        Call call = okHttpClient.newCall(request);

        // 4.执行call
        // 同步阻塞执行
        //      Response response = call.execute();
        // 异步执行
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream inputStream = response.body().byteStream();

                int len = 0;
                File file = new File(Environment.getExternalStorageDirectory(), "imooc666.jpg");
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                byte buf[] = new byte[10240];

                while ((len= inputStream.read(buf)) != -1){
                    fileOutputStream.write(buf,0, len);
                }

                fileOutputStream.flush();
                fileOutputStream.close();
                inputStream.close();

                Log.i(TAG, "Download success");

            }
        });
    }

    public void doDownloadImg(){
        // 1.获取OkHttpClient的对象
//        OkHttpClient okHttpClient = new OkHttpClient();

        // 2.构造发送包
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .get()
                .url(URL + "files/xxx.jpg")   //这个文件名和上传的文件名一样
                .build();

        // 3.获取Call对象
        Call call = okHttpClient.newCall(request);

        // 4.执行call
        // 同步阻塞执行
        //      Response response = call.execute();
        // 异步执行
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream inputStream = response.body().byteStream();
                MainActivity.bitmap = BitmapFactory.decodeStream(inputStream);
                Log.i(TAG, "bitmap : " + MainActivity.bitmap.getWidth() + " x " + MainActivity.bitmap.getHeight());
                Message message = new Message();
                message.what = UPDATE_IMAGE;
                handler.sendMessage(message);
            }
        });
    }

    public void doPostString() {
        // 1.获取OkHttpClient的对象
//        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(MediaType.parse("test/pain;chaset=utf-8"),"{username:hhh,password:xxx}");
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(URL + "postString").post(requestBody).build();

        // 3.获取Call对象
        Call call = okHttpClient.newCall(request);

        // 4.执行call
        // 同步阻塞执行
        //      Response response = call.execute();
        // 异步执行
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                MainActivity.stringBuf = response.body().string();
                Log.d(TAG, MainActivity.stringBuf);
                Message message = new Message();
                message.what = UPDATE_VIEW;
                handler.sendMessage(message);
            }
        });
    }

    public void doPost() throws IOException {

        // 1.获取OkHttpClient的对象
//        OkHttpClient okHttpClient = new OkHttpClient();

        FormBody body = new FormBody.Builder()
                .add("name", "111")
                .add("password", "222")
                .build();
        // 2.构造发送包
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(URL + "login").post(body).build();

        // 3.获取Call对象
        Call call = okHttpClient.newCall(request);

        // 4.执行call
        // 同步阻塞执行
        //      Response response = call.execute();
        // 异步执行
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                MainActivity.stringBuf = response.body().string();
                Log.d(TAG, MainActivity.stringBuf);
                Message message = new Message();
                message.what = UPDATE_VIEW;
                handler.sendMessage(message);
            }
        });
    }


    public void doGet() throws IOException {
        // 1.获取OkHttpClient的对象
//        OkHttpClient okHttpClient = new OkHttpClient();

        // 2.构造发送包
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .get()
                .url(URL + "login?name=dddd&password=gggffgg")
                .build();


        // 3.获取Call对象
        Call call = okHttpClient.newCall(request);

        // 4.执行call
        // 同步阻塞执行
        //      Response response = call.execute();
        // 异步执行
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                MainActivity.stringBuf = response.body().string();
                Log.d(TAG, "string:" + MainActivity.stringBuf);
                Message message = new Message();
                message.what = UPDATE_VIEW;
                handler.sendMessage(message);
            }
        });
    }
}
