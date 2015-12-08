package com.zod.facedetectionjavaandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_contrib.FaceRecognizer;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.Frame;


import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;

// ----------------------------------------------------------------------

public class MainActivity extends Activity {
    private FrameLayout layout;
    private FaceView faceView;
    private Preview mPreview;
    
    
    //Buttons for UI
    Button Recognition;
    Button TrainingButton;
    Button RecognitionDatabase;
    Button Settings;
    Button About;
    EditText trainingName;
    

    //variables for face recognition
    public static String trainingDirectory = null;
    public static String status = "Loading...";
    
    public static FaceRecognizer faceRecognizer;
    public static ArrayList<String> correspondingNames = new ArrayList<String>();
    public static boolean trainingDone = false;
    public static Bitmap testBitmap = null;
    
    //variables for settings algorithm
    public static String algorithm = "Default";  //default is eigen currently
    
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Hide the window title.
    	//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        Recognition = (Button)findViewById(R.id.Recognition);
        TrainingButton = (Button)findViewById(R.id.TrainingButton);
        RecognitionDatabase = (Button)findViewById(R.id.RecognitionDatabase);
        Settings = (Button)findViewById(R.id.Settings);
        About = (Button)findViewById(R.id.About);
        trainingName = (EditText)findViewById(R.id.TrainingName);
           
        RecognitionDatabase.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), Database.class);
                MainActivity.this.startActivity(intent);
				
			}
		});
        
        
        About.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), Aboutc.class);
                MainActivity.this.startActivity(intent);
				
			}
		});
        
        Settings.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(v.getContext(), SettingMain1.class);
                MainActivity.this.startActivity(intent);
	
              
				
			}
		});
        
        TrainingButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Training.trainingName = trainingName.getText().toString();
				
				if(!Training.trainingName.contains("name")){
					Intent intent = new Intent(v.getContext(), Training.class);
					MainActivity.this.startActivity(intent);
				}else{
		            Toast.makeText(getApplicationContext(),"Please enter a name and then press train.", Toast.LENGTH_SHORT).show();

				}
				
			}
		});
        
       Recognition.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			 	
			Intent intent = new Intent(v.getContext(), RealTimeRecognition.class);
            MainActivity.this.startActivity(intent);
		        
		      
			
		}
	});
              
    }
}

// ----------------------------------------------------------------------

class FaceView extends View implements Camera.PreviewCallback {
    public static final int SUBSAMPLING_FACTOR = 4;

    String cascadeFileName = "haarcascade_eye.xml";
    public static IplImage grayImage;
    private CvHaarClassifierCascade classifier;
    private CvMemStorage storage;
    private CvSeq faces;
    private Context context;
    
    private int mDisplayOrientation;
    private int mOrientation;
   
    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void setDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;
        invalidate();
    }
    
    public FaceView(RealTimeRecognition context) throws IOException {
        super(context);
        this.context = context;
        
        AsyncDatabaseTrainer trainer = new AsyncDatabaseTrainer(context);       //opening trained database to compare and reocgnize
        trainer.execute("execute");
          
        classifier = MatUtil.loadClassifier(context, cascadeFileName);

        storage = opencv_core.CvMemStorage.create();
        
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
    	    		
        int f = SUBSAMPLING_FACTOR;
        if (grayImage == null || grayImage.width() != width/f || grayImage.height() != height/f) {
            grayImage = IplImage.create(width/f, height/f, IPL_DEPTH_8U, 1);
                
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
       
        faces = cvHaarDetectObjects(grayImage, classifier, storage, 1.1, 2, CV_HAAR_DO_CANNY_PRUNING);

        Log.d("Face View", "Cascade successfully used on gray images and the faces detected stored.");
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        
        int widt = context.getResources().getDisplayMetrics().widthPixels;
  	  	int heig = context.getResources().getDisplayMetrics().heightPixels;
  	  	System.out.println("zod h " + heig);
        
        paint.setTextSize((float) widt / 43.5f);

       // String s = "FacePreview - This side up.";
        float textWidth = paint.measureText(MainActivity.status);
        canvas.drawText(MainActivity.status, (getWidth() - textWidth) / 2, heig / 24, paint);

        if (faces != null)
        {
            int ReyeX = 0; int ReyeY = 0; int LeyeX = 0; int LeyeY = 0;
        	
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.STROKE);
            System.out.println("zod paint " + paint.getTextSize());
            paint.setTextSize((float)widt/43.5f);
            float scaleX = (float)getWidth()/grayImage.width();
            float scaleY = (float)getHeight()/grayImage.height();
            int total = faces.total();
            
            for (int i = 0; i < total; i++)
            {
                CvRect r = new CvRect(cvGetSeqElem(faces, i));
                int x = r.x() , y = r.y(), w = r.width(), h = r.height();

                if     (i==0)    { ReyeX = x + (w/2); ReyeY = y + (h/2);}
                else if(i==1)    { LeyeX = x + (w/2); LeyeY = y + (h/2);}


                             
                //face recognition stuff
                IplImage testImage1 = grayImage.clone();

                cvSetImageROI(testImage1, cvRect(x - 15, y - 50, w + 35, h + 90));
                IplImage resizeImage = IplImage.create(140, 190, testImage1.depth(), testImage1.nChannels());
                opencv_imgproc.cvResize(testImage1, resizeImage, opencv_imgproc.CV_INTER_AREA);

                Mat testImage = new Mat(resizeImage);

                if(MainActivity.trainingDone == true)
                {
                      int[] plabel = new int[1];
                      double[] pconfidence = new double[1];
                      //TODO either the facerecognizer not working or the testing images not correct
                      MainActivity.faceRecognizer.predict(testImage, plabel, pconfidence);
                      int predictedLabelNew = plabel[0];
                      double confidence = pconfidence[0];

                      String AdjustedName = null;
                      for(int a = 0; a<=(MainActivity.correspondingNames.size()-1);a++)
                        {
                            int num = Integer.parseInt(MainActivity.correspondingNames.get(a).split("\\-")[0]);
                            if(num == predictedLabelNew)
                            {
                                String name = MainActivity.correspondingNames.get(a).split("\\-")[1];
                                AdjustedName = name.split("\\_")[0];

                                break;
                            }
                        }
                          //299 was got after running some tests to get the number to 8 on moto g device
                          //same with height it was done to get a specific number on moto g
                          int widtht = context.getResources().getDisplayMetrics().widthPixels;
                          int heightt = context.getResources().getDisplayMetrics().heightPixels;
                      if(predictedLabelNew != -1 ){

                          canvas.drawText(AdjustedName, (int)(x * (widtht/299)), (int)(y * (heightt/180) -(heightt/72)), paint);
                        }
                        else{
                            canvas.drawText("Unknown", (int)(x * (widtht/299)), (int)(y * (heightt/180) -(heightt/72)), paint);

                        }
                      //}

                  }
                   canvas.drawRect((x)*scaleX, (y)*scaleY, (x+w)*scaleX, (y+h)*scaleY, paint);

            }


            if(ReyeX!=0 & ReyeY!=0)
                canvas.drawCircle((float)((ReyeX+2)*scaleX),(float)(ReyeY*scaleY),5,paint);

            if(LeyeX!=0 & LeyeY!=0)
                canvas.drawCircle((float)((LeyeX+2)*scaleX),(float)(LeyeY*scaleY),5,paint);

        }

        Log.d("Face View", "Overlay for faces successfully drawn.");
    }
    
    

}

// ----------------------------------------------------------------------

class Preview extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder mHolder;
    Camera mCamera;
    Camera.PreviewCallback previewCallback;

    Preview(Context context, Camera.PreviewCallback previewCallback) {
        super(context);
        this.previewCallback = previewCallback;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        Log.d("Camera Preview", "Preview object successfully created.");
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        try {
           mCamera.setPreviewDisplay(holder);
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
            // TODO: add more exception handling logic here
            Log.d("Camera Preview", "preview display could not be set on camera.");
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }


    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Camera.Parameters parameters = mCamera.getParameters();

        List<Size> sizes = parameters.getSupportedPreviewSizes();
        Size optimalSize = getOptimalPreviewSize(sizes, w, h);

       // parameters.setPreviewSize(optimalSize.width, optimalSize.height);

        parameters.setPreviewSize(optimalSize.height, optimalSize.width);
        mCamera.setDisplayOrientation(90);	// new code

        mCamera.setParameters(parameters);
        if (previewCallback != null) {
            mCamera.setPreviewCallbackWithBuffer(previewCallback);
            Camera.Size size = parameters.getPreviewSize();
            byte[] data = new byte[size.width*size.height*
                    ImageFormat.getBitsPerPixel(parameters.getPreviewFormat())/8];
            mCamera.addCallbackBuffer(data);
        }
        mCamera.startPreview();

        Log.d("Camera Preview", "Camera preview successfully started.");
    }


    
}