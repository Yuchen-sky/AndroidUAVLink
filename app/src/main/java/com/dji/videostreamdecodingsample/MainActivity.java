package com.example.luyuchen.getnetwork;
import android.app.Activity;
import android.hardware.Camera;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.gson.Gson;



public class MainActivity extends Activity implements SurfaceHolder.Callback,
        View.OnClickListener, Camera.PictureCallback{
    //---------------------------------------------------------------------------------------
    //Global parameters
    //---------------------------------------------------------------------------------------
    private String TAG="test";
    private String URL="http://192.168.43.227:5000/";



    //---------------------------------------------------------------------------------------
    //Basic functions
    //---------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPhotoError();
        initView();
        initEvent();
        initphoto(savedInstanceState);
        initInform();



        Log.e("kwwl","response.code()==11111111111111111111111111");
        getDatasync();
        getDataAsync();
        deFlightController();
        initFlightController();
    }

    //---------------------------------------------------------------------------------------
    //Inform function
    //---------------------------------------------------------------------------------------
    private static final int MSG_WHAT_SHOW_TOAST = 0;
    private static final int MSG_WHAT_UPDATE_TITLE = 1;
    private static final int MSG_WHAT_UPDATE_CONTENT = 2;
    private TextView titleTv;
    private TextView content;
    private Button secondAction;
    private void initInform(){
        titleTv = (TextView) findViewById(R.id.titleTv);
        content=(TextView) findViewById(R.id.content);
        secondAction = (Button) findViewById(R.id.second_action);

        secondAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateTitle("inform successful");
            }
        });
    }
    public Handler mainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_SHOW_TOAST:
                    Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_WHAT_UPDATE_TITLE:
                    if (titleTv != null) {
                        titleTv.setText((String) msg.obj);
                    }
                    break;
                case MSG_WHAT_UPDATE_CONTENT:
                    if (content != null) {
                        content.setText((String) msg.obj);
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private void showToast(String s) {
        mainHandler.sendMessage(
                mainHandler.obtainMessage(MSG_WHAT_SHOW_TOAST, s)
        );
    }
    private void updateTitle(String s) {
        mainHandler.sendMessage(
                mainHandler.obtainMessage(MSG_WHAT_UPDATE_TITLE, s)
        );
    }
    private void updateContent(String s) {
        mainHandler.sendMessage(
                mainHandler.obtainMessage(MSG_WHAT_UPDATE_CONTENT, s)
        );
    }



    //---------------------------------------------------------------------------------------
    //Based test
    //---------------------------------------------------------------------------------------
    public void getDatasync(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象
                    Request request = new Request.Builder()
                            .url("http://www.baidu.com")//请求接口。如果需要传参拼接到接口后面。
                            .build();//创建Request 对象
                    Response response = null;
                    response = client.newCall(request).execute();//得到Response 对象
                    if (response.isSuccessful()) {
                        Log.e("TAG","response.code()=="+response.code());
                        Log.e("TAG","response.message()=="+response.message());
                        Log.e("TAG","res=="+response.body().string());
                        //此时的代码执行在子线程，修改UI的操作请使用handler跳转到UI线程。
                    }
                    Log.e("kwwl","done");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("kwwl","fail");
                }
            }
        }).start();
    }
    private void getDataAsync() {
        OkHttpClient client = new OkHttpClient();
        Log.e("kwwl","yibu");
        Request request = new Request.Builder()
                .url(URL)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG,"获取数据失败");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){//回调的方法执行在子线程。
                    Log.e(TAG,"获取数据成功了");
                    Log.e(TAG,"response.code()=="+response.code());
                    Log.e(TAG,"response.body().string()=="+response.body().string());
                }
            }
        });
    }



    //---------------------------------------------------------------------------------------
    //Camera test
    //---------------------------------------------------------------------------------------
    private SurfaceView mSurfaceView;
    private Button mIvStart;
    private TextView mTvCountDown;

    private SurfaceHolder mHolder;

    private Camera mCamera;

    private Handler mHandler = new Handler();

    private int mCurrentTimer = 1;

    private boolean mIsSurfaceCreated = false;
    private boolean mIsTimerRunning = false;
    private boolean mIsButtonOfUpload=false;

    private static final int CAMERA_ID = 0; //后置摄像头
//    private static final int CAMERA_ID = 1; //前置摄像头

    @Override
    protected void onPause() {
        super.onPause();

        stopPreview();
    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mIvStart = (Button) findViewById(R.id.start);
        mTvCountDown = (TextView) findViewById(R.id.count_down);
    }
    private SendImageTask sendImageTask;
    private Timer sendImageTimer;

    class SendImageTask extends TimerTask {

        @Override
        public void run() {
            if (mIsButtonOfUpload) {
                mCamera.takePicture(null, null, null, MainActivity.this);
                playSound();

            }

        }

    }

    private void initEvent() {
       mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);

        mIvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsButtonOfUpload==true) mIsButtonOfUpload=false;
                else mIsButtonOfUpload=true;
            }
        });
        if (sendImageTimer == null){
            sendImageTimer = new Timer();
            sendImageTask = new SendImageTask();
            sendImageTimer.schedule(sendImageTask,100, 1000);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsSurfaceCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsSurfaceCreated = false;
    }

    private void startPreview() {
        if (mCamera != null || !mIsSurfaceCreated) {
            Log.e(TAG, "startPreview will returnn");
            return;
        }

        mCamera = Camera.open(CAMERA_ID);

        Camera.Parameters parameters = mCamera.getParameters();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;


        Camera.Size size = getBestPreviewSize(width, height, parameters);
        Log.e(TAG, "宽为："+Integer.toString(width)+" 长为："+Integer.toString(height));
        if (true) {
            //设置预览分辨率
         //parameters.setPreviewSize(100, 100);
            //设置保存图片的大小
           parameters.setPictureSize(2992, 2992);
        }

        //自动对焦
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
       parameters.setPreviewFrameRate(20);

        //设置相机预览方向
        mCamera.setDisplayOrientation(90);

        mCamera.setParameters(parameters);

        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        mCamera.startPreview();
    }
    @Override
    public void onClick(View v) {

    }
    private void stopPreview() {
        //释放Camera对象
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height,
                                           Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }

        return result;
    }




/**
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (mCurrentTimer > 0) {
                mTvCountDown.setText(mCurrentTimer + "");

                mCurrentTimer--;
                mHandler.postDelayed(timerRunnable, 100);
            } else {
                mTvCountDown.setText("");

                mCamera.takePicture(null, null, null, MainActivity.this);
                playSound();

                mIsTimerRunning = false;
                mCurrentTimer = 1;
            }
        }
    };**/

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        File file=new File
                ("/mnt/sdcard/drone" + File.separator +
                        System.currentTimeMillis() + ".jpg");
       /** if (file.exists()) {
            file.delete();
            Log.e(TAG,"stream文件存在与删除");
        }**/
        try {

            FileOutputStream fos = new FileOutputStream(file);
            /** FileOutputStream fos = new FileOutputStream(new File
             ("/mnt/sdcard/drone" + File.separator +
             System.currentTimeMillis() + ".jpg"));**/

            //旋转角度，保证保存的图片方向是对的
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            matrix.setRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap=scaleBitmap(bitmap,300,300);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            onYuvDataReceived(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        mCamera.startPreview();
    }

    /**
     *   播放系统拍照声音
     */
    public void playSound() {
      /**
        MediaPlayer mediaPlayer = null;
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int volume = audioManager.getStreamVolume( AudioManager.STREAM_NOTIFICATION);

        if (volume != 0) {
            if (mediaPlayer == null)
                mediaPlayer = MediaPlayer.create(this,
                        Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
            if (mediaPlayer != null) {
                mediaPlayer.start();
            }
        }
       **/
        showToast("系统成功拍照");
        Log.e(TAG,"系统成功拍照");
    }







    //---------------------------------------------------------------------------------------
    //Taken and process photos
    //---------------------------------------------------------------------------------------
    public static final int TAKE_PHOTO = 1;
    public static final int CROP_PHOTO = 2;
    private Button takePhoto;
    private ImageView picture;
    private Uri imageUri;

    // android 7.0系统解决拍照的问题
    private void initPhotoError(){
        // android 7.0系统解决拍照的问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        //builder.detectFileUriExposure();
    }

    protected void initphoto(Bundle savedInstanceState) {
        // android 7.0系统解决拍照的问题
        //StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        //StrictMode.setVmPolicy(builder.build());
        initPhotoError();


        takePhoto = (Button) findViewById(R.id.take_photo);
        picture = (ImageView) findViewById(R.id.picture);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            // 创建File对象，用于存储拍照后的图片
                File outputImage = new File("/mnt/sdcard/drone/", "tempImage.jpg");
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                        Log.e(TAG,"文件存在与删除");
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageUri = Uri.fromFile(outputImage);
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                Log.e(TAG,"调用相机");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO); // 启动相机程序
            }
        });
    }

    /**
     * 根据给定的宽和高进行拉伸
     */
    private Bitmap scaleBitmap(Bitmap origin, int newWidth, int newHeight) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);// 使用后乘
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (!origin.isRecycled()) {
            origin.recycle();
        }
        return newBM;
    }

    /**
     * 按比例缩放图片
     */
    private Bitmap scaleBitmap(Bitmap origin, float ratio) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    //返回活动处理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG,"活动返回与获取");
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream
                                (getContentResolver()
                                        .openInputStream(imageUri));


                        bitmap=scaleBitmap(bitmap,100,100);
                        saveJPG_After(bitmap, "/mnt/sdcard/drone/test2.jpg");
                        picture.setImageBitmap(bitmap); // 将裁剪后的照片显示出来
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    onYuvDataReceived();
                    showToast("successful");
                  //  Intent intent = new Intent("com.android.camera.action.CROP");
                  //  intent.setDataAndType(imageUri, "image/*");
                  //  intent.putExtra("scale", true);
                 //  intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                 //   startActivityForResult(intent, CROP_PHOTO); // 启动裁剪程序
                }
                break;
            case CROP_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream
                                (getContentResolver()
                                        .openInputStream(imageUri));
                        picture.setImageBitmap(bitmap); // 将裁剪后的照片显示出来
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    onYuvDataReceived();
                }

                break;
            default:
                break;
        }
    }




    //---------------------------------------------------------------------------------------
    //Upload images
    //---------------------------------------------------------------------------------------
    private String uploadResult;

    public String uploadImage(String URL, File file) {
        // MultipartBuilder，是上传文件的query
        // addFormDataPart方法：@param [String]name, [String]value
        // addFormDataPart方法：@param [String]name, [String]fileName, [String]fileType, [String]file
        OkHttpClient client=new OkHttpClient();

        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files",
                        file.getName(),
                        RequestBody.create(MediaType.parse("image/jpeg"), file))
                .build();

        // request方法： @param [String]URL, [RequestBody]requestBody
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();

        // response储存服务器的回应
        final File deletefile = file;
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //showToast("fail");
                Log.e(TAG,"上传失败");
                deletefile.delete();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                uploadResult = response.body().string();
                Log.e(TAG,"上传有回声");
                deletefile.delete();
            }
        });
        // 把response转换成string

        return uploadResult;
    }

    public static void saveJPG_After(Bitmap bitmap, String name) {
        File file = new File(name);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onYuvDataReceived() {
        //In this demo, we test the YUV data by saving it into JPG files.
        //DJILog.d(TAG, "onYuvDataReceived " + dataSize);
        if (true) {
            //if (yuvFrame != null) {

            Log.e(TAG, "onYuvDataReceived2222222222上传stream222222 " );
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                   // final String savepath = saveYuvDataToJPEG(bytes, width, height);
                    uploadImage(URL+"uploadImage",new File("/mnt/sdcard/drone/stream.jpg"));


                }
            });
        }
    }
    public void onYuvDataReceived(final File file) {
        //In this demo, we test the YUV data by saving it into JPG files.
        //DJILog.d(TAG, "onYuvDataReceived " + dataSize);
        if (true) {
            //if (yuvFrame != null) {

            Log.e(TAG, "onYuvDataReceived2222222222上传stream222222 " );
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    // final String savepath = saveYuvDataToJPEG(bytes, width, height);
                    uploadImage(URL+"uploadImage", file);


                }
            });
        }
    }
















    //---------------------------------------------------------------------------------------
    //about control link
    //---------------------------------------------------------------------------------------
    private ReciveControlDataTask reciveControlDataTask;
    private Timer mReciveControlDataTimer;
    private float gPitch;
    private float gRoll;
    private float gYaw;

    private float mPitch;
    private float mRoll;
    private float mYaw;
    private float mThrottle;
    private float middle;
    private float right;
    private float left;
    private String testString;

    private void deFlightController(){
        gPitch=0;
        gRoll=0;
        gYaw=0;
        mPitch=0;
        mRoll=0;
        mYaw=0;
        mThrottle=0;
        middle=1;
        right=0;
        left=0;
        //mReciveControlDataTimer.cancel();
        mReciveControlDataTimer=null;
        reciveControlDataTask = null;

    }

    private void initFlightController() {

            gPitch=0;
            gRoll=0;
            gYaw=0;
            mPitch=0;
            mRoll=0;
            mYaw=0;
            mThrottle=0;
            //定时器用来定时，200ms发送一次数据
         //   if (null == mSendVirtualStickDataTimer) {
           //     mSendVirtualStickDataTask = new SendVirtualStickDataTask();
               // mSendVirtualStickDataTimer = new Timer();
             //   mSendVirtualStickDataTimer.schedule(mSendVirtualStickDataTask, 100, 200);
            //}
       if (mReciveControlDataTimer == null){
                mReciveControlDataTimer = new Timer();
                reciveControlDataTask = new ReciveControlDataTask();
                mReciveControlDataTimer.schedule(reciveControlDataTask,100, 500);
            }

    }

    class ReciveControlDataTask extends TimerTask {

        private Gson gson;
        @Override
        public void run() {
            final String address = URL+"getcontrol";
            if(gson==null){
                gson = new Gson();
            }
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(address)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {


                    gPitch=0;
                    gRoll=0;
                    gYaw=0;
                    mPitch=0;
                    mRoll=0;
                    mYaw=0;
                    mThrottle=0;
                    Log.e("TAG","fail control");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //得到服务器返回的具体内容
                    String responseData=response.body().string();
                    ControlData control = gson.fromJson(responseData, ControlData.class);
                    mPitch = control.getmPitch();
                    mRoll = control.getmRoll();
                    mYaw = control.getmYaw();
                    mThrottle = control.getmThrottle();
                    gPitch = control.getgPitch();
                    gRoll = control.getgPitch();
                    gYaw = control.getgYaw();
                    middle=control.getmiddle();
                    right=control.getright();
                    left=control.getleft();
                    testString=control.gettestString();
                    updateTitle(testString);
                    String t=Float.toString(middle);
                    updateContent(" middle"+Float.toString(middle)+" left"+Float.toString(left)+" right"+Float.toString(right));
                    Log.e("TAG","success control"+ control.getmPitch());
                }

            });

        }

    }















    //---------------------------------------------------------------------------------------
    //may be used in the future
    //---------------------------------------------------------------------------------------
    private String saveYuvDataToJPEG(byte[] yuvFrame, int width, int height){
        if (yuvFrame.length < width * height) {
            //DJILog.d(TAG, "yuvFrame size is too small " + yuvFrame.length);
            return "";
        }

        byte[] y = new byte[width * height];
        byte[] u = new byte[width * height / 4];
        byte[] v = new byte[width * height / 4];
        byte[] nu = new byte[width * height / 4]; //
        byte[] nv = new byte[width * height / 4];

        System.arraycopy(yuvFrame, 0, y, 0, y.length);
        for (int i = 0; i < u.length; i++) {
            v[i] = yuvFrame[y.length + 2 * i];
            u[i] = yuvFrame[y.length + 2 * i + 1];
        }
        int uvWidth = width / 2;
        int uvHeight = height / 2;
        for (int j = 0; j < uvWidth / 2; j++) {
            for (int i = 0; i < uvHeight / 2; i++) {
                byte uSample1 = u[i * uvWidth + j];
                byte uSample2 = u[i * uvWidth + j + uvWidth / 2];
                byte vSample1 = v[(i + uvHeight / 2) * uvWidth + j];
                byte vSample2 = v[(i + uvHeight / 2) * uvWidth + j + uvWidth / 2];
                nu[2 * (i * uvWidth + j)] = uSample1;
                nu[2 * (i * uvWidth + j) + 1] = uSample1;
                nu[2 * (i * uvWidth + j) + uvWidth] = uSample2;
                nu[2 * (i * uvWidth + j) + 1 + uvWidth] = uSample2;
                nv[2 * (i * uvWidth + j)] = vSample1;
                nv[2 * (i * uvWidth + j) + 1] = vSample1;
                nv[2 * (i * uvWidth + j) + uvWidth] = vSample2;
                nv[2 * (i * uvWidth + j) + 1 + uvWidth] = vSample2;
            }
        }
        //nv21test
        byte[] bytes = new byte[yuvFrame.length];
        System.arraycopy(y, 0, bytes, 0, y.length);
        for (int i = 0; i < u.length; i++) {
            bytes[y.length + (i * 2)] = nv[i];
            bytes[y.length + (i * 2) + 1] = nu[i];
        }
        Log.d(TAG,
                "onYuvDataReceived: frame index: "

                        + ",array length: "
                        + bytes.length);

        return screenShot(bytes, Environment.getExternalStorageDirectory() + "/DJI_ScreenShot", width, height);
    }
    /**
     * Save the buffered data into a JPG image file
     */
    private String screenShot(byte[] buf, String shotDir, int width, int height) {
        File dir = new File(shotDir);
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }
        YuvImage yuvImage = new YuvImage(buf,
                ImageFormat.NV21,
                width,
                height,
                null);
        OutputStream outputFile;
        final String path = dir + "/ScreenShot_" + System.currentTimeMillis() + ".jpg";
        try {
            outputFile = new FileOutputStream(new File(path));
        } catch (FileNotFoundException e) {
            Log.e(TAG, "test screenShot: new bitmap output file error: " + e);
            return "";
        }
        if (outputFile != null) {
            yuvImage.compressToJpeg(new Rect(0,
                    0,
                    width,
                    height), 50, outputFile);
        }
        try {
            outputFile.close();
        } catch (IOException e) {
            Log.e(TAG, "test screenShot: compress yuv image error: " + e);
            e.printStackTrace();
        }
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                displayPath(path);
//            }
//        });
        return path;
    }
}
