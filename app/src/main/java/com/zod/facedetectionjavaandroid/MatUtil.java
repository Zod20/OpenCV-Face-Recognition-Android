package com.zod.facedetectionjavaandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.bytedeco.javacpp.opencv_core.cvLoad;

/**
 * Created by Zodiac on 12/11/2015.
 */
public class MatUtil {

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    public static opencv_objdetect.CvHaarClassifierCascade loadClassifier(Context context, String cascadeName)
    {
        opencv_objdetect.CvHaarClassifierCascade classifier;


        boolean isCopyCompleted = CopyFile(context, cascadeName);
        Log.e("", "isCopyCompleted:::" + isCopyCompleted);

        File classifierFile = new File(context.getCacheDir(),
                cascadeName);

        if (classifierFile == null || classifierFile.length() <= 0) {
                System.out.println("zod12/10 " + "classifier length negative so error");
        }

        // Preload the opencv_objdetect module to work around a known bug.
        Loader.load(opencv_objdetect.class);
        classifier = new opencv_objdetect.CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
        classifierFile.delete();
        if (classifier.isNull()) {
            System.out.println("zod12/10 " + "classifier is null error");
        }


        System.out.println("zod12/10 " + "classifier found and loaded successfully");
        return classifier;
    }


    private static boolean CopyFile(Context context,String pathName) {

        boolean isCopyCompleted = false;

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = context.getResources().getAssets().open(
                    pathName);
            File outFile = new File(context.getCacheDir(),
                    pathName);
            if (!outFile.exists()) {
                outFile.createNewFile();
            }

            outputStream = new FileOutputStream(outFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            // read from is to buffer
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                Log.v("", "data..." + bytesRead);
            }

            isCopyCompleted = true;
            inputStream.close();
            // flush OutputStream to write any buffered data to file
            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            isCopyCompleted = false;
            e.printStackTrace();
        }
        return isCopyCompleted;

    }
}
