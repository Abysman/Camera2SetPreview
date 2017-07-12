package test.com.camera2setpreview;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.MultipartFormDataBody;


@SuppressLint("NewApi") public class MainActivity extends Activity {
    //每秒拍照
    private int cameraTime = 5;

    private Timer mTimer;

    //TTS对象
    private TextToSpeech textToSpeech;

    private Camera camera;
    private boolean preview  = false ;
    private Button button ,camera_start,camera_stop;
    private int cameraPosition;
    private SurfaceHolder  holder;
    private TextView textView;

    private Thread mthread;
    private String infoBack;

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.obj==null){
                textView.setText("");
            }
            else{
                textView.setText(msg.obj.toString());
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initListeners();


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//拍照过程屏幕一直处于高亮
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        SurfaceView surfaceView =  (SurfaceView) findViewById(R.id.surfaceView);
        holder =surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.setFixedSize(1600, 960);
        holder.addCallback(new SurfaceViewCallback());

        camera_start =(Button)findViewById(R.id.camera);
        camera_stop = (Button)findViewById(R.id.camera_stop);


        camera_stop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTimer!=null){
                    mTimer.cancel();
                    mTimer.purge();
                    mTimer = null;
                }

            }
        });

        camera_start.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(mTimer == null){
                    mTimer = new Timer();
                }
                mTimer.schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
//                        camera.takePicture(null,null,jpeg);
                        camera.autoFocus(new AutoFocusCallback() {//自动对焦
                            @Override
                            public void onAutoFocus(boolean success, Camera camera) {
                                // TODO Auto-generated method stub
                                if(success) {
                                    //设置参数，并拍照
                                    Parameters params = camera.getParameters();
                                    params.setPictureFormat(PixelFormat.JPEG);//图片格式
                                    params.setPreviewSize(800, 480);//图片大小
                                    camera.setParameters(params);//将参数设置到我的camera
                                    camera.takePicture(null, null, jpeg);//将拍摄到的照片给自定义的对象
                                }
                            }
                        });
                    }
                },0,cameraTime*1000);

                camera.autoFocus(new AutoFocusCallback() {//自动对焦
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        // TODO Auto-generated method stub
                        if(success) {
                            //设置参数，并拍照
                            Parameters params = camera.getParameters();
                            params.setPictureFormat(PixelFormat.JPEG);//图片格式
                            params.setPreviewSize(800, 480);//图片大小
                            camera.setParameters(params);//将参数设置到我的camera
                            camera.takePicture(null, null, jpeg);//将拍摄到的照片给自定义的对象
                        }
                    }
                });
            }
        });
//        button =(Button)findViewById(R.id.button);
//        button.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                // TODO Auto-generated method stub
//                int cameraCount = 0;
//                CameraInfo cameraInfo = new CameraInfo();
//                cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数
//
//                for(int i = 0; i < cameraCount; i ++  ) {
//                    Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
//                    if(cameraPosition == 1) {
//                        //现在是后置，变更为前置
//                        if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
//                            camera.stopPreview();//停掉原来摄像头的预览
//                            camera.release();//释放资源
//                            camera = null;//取消原来摄像头
//                            camera = Camera.open(i);//打开当前选中的摄像头
//                            try {
//                                camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
//                            } catch (IOException e) {
//                                // TODO Auto-generated catch block
//                                e.printStackTrace();
//                            }
//
////                            setCameraDisplayOrientation(MainActivity.this, cameraPosition, camera);
//                            camera.startPreview();//开始预览
//                            cameraPosition = 0;
//                            break;
//                        }
//                    } else {
//                        //现在是前置， 变更为后置
//                        if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
//                            camera.stopPreview();//停掉原来摄像头的预览
//                            camera.release();//释放资源
//                            camera = null;//取消原来摄像头
//                            camera = Camera.open(i);//打开当前选中的摄像头
//                            try {
//                                camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
//                            } catch (IOException e) {
//                                // TODO Auto-generated catch block
//                                e.printStackTrace();
//                            }
//                            setCameraDisplayOrientation(MainActivity.this, cameraPosition, camera);
//                            camera.startPreview();//开始预览
//                            cameraPosition = 1;
//                            break;
//                        }
//                    }
//                }
//            }
//        });

        textView = (TextView)findViewById(R.id.infoBack);
    }

    private void initListeners() {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        textToSpeech.speak("cannot work", TextToSpeech.QUEUE_FLUSH,
                                null);
                    }

                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if ( textToSpeech!= null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private final class SurfaceViewCallback implements android.view.SurfaceHolder.Callback {
        /**
         * surfaceView 被创建成功后调用此方法
         */
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            camera = Camera.open(cameraPosition); //默认启用的摄像头是后置摄像头id=0，  id =1前置摄像头

            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Camera.Parameters parameters = camera.getParameters();

            setCameraDisplayOrientation(MainActivity.this, cameraPosition, camera);
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE); // 获取当前屏幕管理器对象

            Display display = wm.getDefaultDisplay();
//            parameters.setPictureSize(display.getWidth(), display.getHeight());
            parameters.setPictureFormat(PixelFormat.JPEG);
            parameters.set("jpeg-quality", 100);
//            parameters.setPictureSize(1024,768);
            camera.setParameters(parameters);
            camera.startPreview();
            preview = true;
        }
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
        }
        /**
         * SurfaceView 被销毁时释放掉 摄像头
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if(camera != null) {
                if(preview) {
                    camera.stopPreview();
                    preview = false;
                }
                camera.release();
            }
        }
    }

    PictureCallback jpeg = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            try {
                Matrix matrix = new Matrix();
                matrix.setRotate(90);
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),bitmap.getHeight(), matrix, true);
                UploadPicture(bitmap);
                bitmap.recycle();
                camera.stopPreview();//关闭预览 处理数据
                camera.startPreview();//数据处理完后继续开始预览
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };


    public static void setCameraDisplayOrientation ( Activity activity ,
                                                     int cameraId , android.hardware.Camera camera ) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo ( cameraId , info );
        int rotation = activity.getWindowManager ().getDefaultDisplay ().getRotation ();
        int degrees = 0 ;
        switch ( rotation ) {
            case Surface.ROTATION_0 : degrees = 0 ; break ;
            case Surface.ROTATION_90 : degrees = 90 ; break ;
            case Surface.ROTATION_180 : degrees = 180 ; break ;
            case Surface.ROTATION_270 : degrees = 270 ; break ;
        }

        int result ;
        if ( info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ) {
            result = ( info.orientation + degrees ) % 360 ;
            result = ( 360 - result ) % 360 ;   // compensate the mirror
        } else {   // back-facing
            result = ( info.orientation - degrees + 360 ) % 360 ;
        }
        camera.setDisplayOrientation ( result );
    }

    public void UploadPicture(Bitmap bitmap){
        AsyncHttpPost post = new AsyncHttpPost("http://192.168.43.146:5000");
        File outputImage = new File(Environment.
                getExternalStorageDirectory() + "/" + "output_image.jpeg");
        try{
            if(outputImage.exists()){
                outputImage.delete();
            }
            outputImage.createNewFile();
        }catch(IOException e){
            e.printStackTrace();
        }
        try{
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputImage));
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,bos);
            bos.flush();
            bos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        MultipartFormDataBody body = new MultipartFormDataBody();
        body.addFilePart("userfile", outputImage);
        post.setBody(body);
        AsyncHttpClient.getDefaultInstance().executeString(post, new AsyncHttpClient.StringCallback() {
            @Override
            public void onCompleted(Exception ex, AsyncHttpResponse source, String result) {
                infoBack = result;
                mthread = new Thread(new Runnable(){
                    @Override
                    public void run() {
                        Message message = new Message();
                        message.what = 0;
                        message.obj = infoBack;
                        handler.sendMessage(message);
                    }
                });
                mthread.start();
                TexttoSpeech(result);
                System.out.println("str is :"+result);
            }
        });
    }

    public void TexttoSpeech(String s){
        if (textToSpeech != null && !textToSpeech.isSpeaking()) {
            textToSpeech.setPitch(1.0f);// 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
            textToSpeech.speak(s,
                    TextToSpeech.QUEUE_ADD, null);
        }
    }
}

