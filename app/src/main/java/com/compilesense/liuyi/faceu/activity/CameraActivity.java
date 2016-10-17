package com.compilesense.liuyi.faceu.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import com.compilesense.liuyi.faceu.DetectionBasedTracker;
import com.compilesense.liuyi.faceu.FaceAlignment;
import com.compilesense.liuyi.faceu.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 原本的 openCV 程序是横屏显示的,要改为竖屏显示,对矩阵进行了翻转,所以同时要改变 CacheBitMap 的大小。
 * 并且修改了图片绘制的目标区域。
 */

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "CameraActivity";


    private Mat mRgba;
    private Mat mGray;
    private Mat mFace;
    private Bitmap mFaceBitmap;
    private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);

    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;

    //和脸部检测相关
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;
    private DetectionBasedTracker mNativeDetector;

    private ImageView mFaceView;
    private Point[] mPoints = new Point[68];
    private FaceAlignment mFaceAlignment;

    //openCV 提供的java摄像头预览类
    private CameraBridgeViewBase mCameraView;
    //openC native库加载回调
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {

                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mNativeDetector.start();
                    mCameraView.enableView();//使能cameraView
                } break;

                default: {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initOpenCV();
        initCameraView();
        initKeyPointDetection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if ( mCameraView != null){
            mCameraView.disableView();
        }
    }

    void initKeyPointDetection(){
        mFaceAlignment = FaceAlignment.getInstance();
        mFaceAlignment.init(this);
    }

    void initOpenCV(){
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.e(TAG,"未能成功加载openCV本地库");
        }
    }

    void initViews(){
        mFaceView = (ImageView) findViewById(R.id.img_face);
        initCameraView();
    }

    void initCameraView(){
        mCameraView = (CameraBridgeViewBase) findViewById(R.id.jcv_camera_view);
        mCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mCameraView.setCvCameraViewListener(new CameraBridgeViewBase.MyCameraViewListener() {
            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame, Rect face, Point[] keyPoints) {

                /**
                 * 我并未在openCV源码中看到对 Mat 有释放资源的操作,但是这里对矩阵的转置会产生新的矩阵
                 * 要注意释放资源,否则将会造成 native 层的内存溢出。
                 */
                mRgba = inputFrame.rgba().t();
                mGray = inputFrame.gray().t();

                Core.flip(mRgba, mRgba, 0);//转向
                Core.flip(mRgba, mRgba, 1);//水平翻转

                Core.flip(mGray, mGray, 0);
                Core.flip(mGray, mGray, 1);

                if (mAbsoluteFaceSize == 0) {
                    int height = mGray.rows();
                    if (Math.round(height * mRelativeFaceSize) > 0) {
                        mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
                    }
                    mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
                }

                MatOfRect faces = new MatOfRect();
                mNativeDetector.detect(mGray, faces);

                Rect[] f = faces.toArray();
                if (f.length>0){
                    face.x = f[0].x;
                    face.y = f[0].y;
                    face.width = f[0].width;
                    face.height = f[0].height;

                    if (mFace != null){
                       mFace.release();
                    }
                    try {
                        mFace = mGray.submat(face);
                        Point[] tempPoints = mFaceAlignment.detectKeyPoint(mFace,mPoints);
                        if (tempPoints.length != keyPoints.length){
                            Log.e(TAG,"tempPoints.length != keyPoints.length");
                        }
                        System.arraycopy(tempPoints, 0, keyPoints, 0, keyPoints.length);

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    Imgproc.rectangle(mRgba, face.tl(), face.br(), FACE_RECT_COLOR, 3);
                }else {
                    face.x = 0;
                    face.y = 0;
                    face.width = 0;
                    face.height = 0;
                }
                //释放资源
                mGray.release();
//                mRgba.release();
                return mRgba;
            }

            @Override
            public void onCameraViewStarted(int width, int height) {
                mGray = new Mat();
                mRgba = new Mat();
            }

            @Override
            public void onCameraViewStopped() {
                mGray.release();
                mRgba.release();
            }

            /**
             * 旋转图片
             if (mCameraOrientation == 270) {
             // Rotate clockwise 270 degrees
             Core.flip(src.t(), dst, 0);
             } else if (mCameraOrientation == 180) {
             // Rotate clockwise 180 degrees
             Core.flip(src, dst, -1);
             } else if (mCameraOrientation == 90) {
             // Rotate clockwise 90 degrees
             Core.flip(src.t(), dst, 1);
             } else if (mCameraOrientation == 0) {
             // No rotation
             dst = src;
             }
             */

            //我进行了修改这么函数不会被回调了
            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

                mRgba = inputFrame.rgba();
                mGray = inputFrame.gray();

                Mat mRgbaT = mRgba.t();
                Core.flip(mRgba.t(), mRgbaT, 0);//转向
                Core.flip(mRgbaT,mRgbaT,1);//水平翻转
                Mat mGrayT = mGray.t();
                Core.flip(mGray.t(), mGrayT, 0);
                Core.flip(mGrayT,mGrayT,1);

                if (mAbsoluteFaceSize == 0) {
                    int height = mGrayT.rows();
                    if (Math.round(height * mRelativeFaceSize) > 0) {
                        mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
                    }
                    mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
                }

                MatOfRect faces = new MatOfRect();

                if (mNativeDetector != null){
                    mNativeDetector.detect(mGrayT, faces);
                } else {
                    Log.e(TAG, "Detection method is not selected!");
                }

                Rect[] facesArray = faces.toArray();

                for (int i = 0; i < facesArray.length; i++){
                    Imgproc.rectangle(mRgbaT, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
                }
                return mRgbaT;
            }

        });
    }

    private Thread mKeyPointsDetectionThread = new Thread(new KeyPointsDetectWorker());
    private boolean oneFramePrepared = false;

    class KeyPointsDetectWorker implements Runnable{
        @Override
        public void run() {

        }
    }
}
