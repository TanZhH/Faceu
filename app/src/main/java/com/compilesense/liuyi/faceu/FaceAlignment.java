package com.compilesense.liuyi.faceu;

import android.content.Context;
import android.provider.MediaStore;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by shenjingyuan002 on 16/10/12.
 */

public class FaceAlignment {
    private static final String TAG = "FaceAlignment";

    private FaceAlignment(){
        System.loadLibrary("facedetectionkeypoint-lib");
    }
    private static class Singleton{
        static private FaceAlignment Instance = new FaceAlignment();
    }
    public static FaceAlignment getInstance() {
        return Singleton.Instance;
    }

    public Point[] detectKeyPoint(Mat img, Point[] points){
        points = detectKeyPoints(img.getNativeObjAddr(),points);
//        for (int i = 0; i < points.length; i++){
//            Log.d(TAG,"sqwd: 得到:ps["+i+"]:"+points[i]);
//        }
        return points;
    }

    public void init(Context context){
        loadModelFile(context,"lbf.model",R.raw.lbf);
        String dir = loadModelFile(context,"regressor.model",R.raw.regressor);
        Log.d(TAG,"getAbsolutePath:"+dir);
        File fDir = new File(dir);
        String[] strings = fDir.list();
        for (String s : strings){
            Log.d(TAG,"目录下的文件:" + s);
            File file = new File(fDir,s);
            Log.d(TAG,"大小:"+file.length());
        }
        dir = dir+"/";
        initModel(dir);
    }

    private String loadModelFile(Context context, String modelName, int resourceId){
        InputStream is = context.getResources().openRawResource(resourceId);

        File modelDir = context.getDir("model",Context.MODE_PRIVATE);
        File modelFile = new File(modelDir,modelName);

        try {
            FileOutputStream os = new FileOutputStream(modelFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            is.close();
            os.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return modelDir.getAbsolutePath();
    }

    private native void initModel(String modelName);
    private native Point[] detectKeyPoints(long image, Point[] points);
}
