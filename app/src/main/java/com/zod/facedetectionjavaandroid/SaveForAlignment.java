package com.zod.facedetectionjavaandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvFlip;
import static org.bytedeco.javacpp.opencv_core.cvGetSeqElem;
import static org.bytedeco.javacpp.opencv_core.cvLoad;
import static org.bytedeco.javacpp.opencv_core.cvRect;
import static org.bytedeco.javacpp.opencv_core.cvSetImageROI;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;

public class SaveForAlignment extends Activity {


    private FrameLayout layout;
    private AlignmentView trainView1;
    private Preview mPreview;
    public static String trainingName = null;
    public static opencv_core.Mat MatFace = null;
    String s = "Press on the screen to save your image as " +  trainingName + " in the database.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    class AlignmentView extends View implements Camera.PreviewCallback {
        public static final int SUBSAMPLING_FACTOR = 4;

        public opencv_core.IplImage grayImage;					//was static in faceView
        private opencv_objdetect.CvHaarClassifierCascade classifier;
        private opencv_core.CvMemStorage storage;
        private opencv_core.CvSeq faces;
        private Context context;


        public AlignmentView(Training context) throws IOException {
            super(context);
            this.context = context;



            boolean isCopyCompleted = CopyFile();
            Log.e("", "isCopyCompleted:::" + isCopyCompleted);


            File classifierFile = new File(context.getCacheDir(),
                    "haarcascade_frontalface_alt2.xml");

            if (classifierFile == null || classifierFile.length() <= 0) {
                throw new IOException(
                        "Could not extract the classifier file from Java resource.");
            }

            // Preload the opencv_objdetect module to work around a known bug.
            Loader.load(opencv_objdetect.class);
            classifier = new opencv_objdetect.CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
            classifierFile.delete();
            if (classifier.isNull()) {
                throw new IOException("Could not load the classifier file.");
            }
            storage = opencv_core.CvMemStorage.create();

            Log.d("Classifier Status", "Classifier found and successfully loaded.");

        }


        private boolean CopyFile() {

            boolean isCopyCompleted = false;

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                inputStream = getResources().getAssets().open(
                        "haarcascade_frontalface_alt2.xml");
                File outFile = new File(context.getCacheDir(),
                        "haarcascade_frontalface_alt2.xml");
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


        public void onPreviewFrame(final byte[] data, final Camera camera) {
            try {
                Camera.Size size = camera.getParameters().getPreviewSize();
                processImage(data, size.width, size.height);
                camera.addCallbackBuffer(data);
            } catch (RuntimeException e) {
                // The camera has probably just been released, ignore.
            }
        }

        protected void processImage(byte[] data, int width, int height) {
            // First, downsample our image and convert it into a grayscale IplImage


            //TODO change the subsampling factor to increase quality
            int f = SUBSAMPLING_FACTOR;
            if (grayImage == null || grayImage.width() != width/f || grayImage.height() != height/f) {
                grayImage = opencv_core.IplImage.create(width / f, height / f, IPL_DEPTH_8U, 1);


            }
            int imageWidth  = grayImage.width();
            int imageHeight = grayImage.height();
            int dataStride = f*width;
            int imageStride = grayImage.widthStep();
            ByteBuffer imageBuffer = grayImage.getByteBuffer();
            for (int y = 0; y < imageHeight; y++) {
                int dataLine = y*dataStride;
                int imageLine = y*imageStride;
                for (int x = 0; x < imageWidth; x++) {
                    imageBuffer.put(imageLine + x, data[dataLine + f*x]);
                }
            }



            Log.d("Face View", "Preview image successfully converted to gray image.");

            cvClearMemStorage(storage);
            cvFlip(grayImage, grayImage, 1);


            faces = cvHaarDetectObjects(grayImage, classifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
            Log.d("Face View", "Cascade successfully used on gray images and the faces detected stored.");
            postInvalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setTextSize(20);

            //TODO show error message if the default enter your name is there and train is pressed
            //also show error message if the name entered contains space


            float textWidth = paint.measureText(s);
            canvas.drawText(s, (getWidth()-textWidth)/2, 20, paint);

            if (faces != null)
            {

                paint.setStrokeWidth(2);
                paint.setStyle(Paint.Style.STROKE);
                float scaleX = (float)getWidth()/grayImage.width();
                float scaleY = (float)getHeight()/grayImage.height();
                int total = faces.total();


                for (int i = 0; i < total; i++)
                {

                    opencv_core.CvRect r = new opencv_core.CvRect(cvGetSeqElem(faces, i));
                    int x = r.x() , y = r.y(), w = r.width(), h = r.height();

                    opencv_core.IplImage testImage1 = grayImage.clone();

                    cvSetImageROI(testImage1, cvRect(x, y, w, h));
                    opencv_core.IplImage resizeImage = opencv_core.IplImage.create(100, 100, testImage1.depth(), testImage1.nChannels());
                    opencv_imgproc.cvResize(testImage1, resizeImage, opencv_imgproc.CV_INTER_AREA);

                    MatFace = new opencv_core.Mat(resizeImage);

                    canvas.drawRect(x*scaleX, y*scaleY, (x+w)*scaleX, (y+h)*scaleY, paint);
                }
            }

            Log.d("Face View", "Overlay for faces successfully drawn.");
        }



        public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
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
    }


}
