package com.zod.facedetectionjavaandroid;

import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvFlip;
import static org.bytedeco.javacpp.opencv_core.cvGetSeqElem;
import static org.bytedeco.javacpp.opencv_core.cvLoad;
import static org.bytedeco.javacpp.opencv_core.cvRect;
import static org.bytedeco.javacpp.opencv_core.cvSetImageROI;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_highgui;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;



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
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class Training extends Activity {

	private FrameLayout layout;
    private TrainView trainView;
    private Preview mPreview;
	public static String trainingName = null;
	public static Mat MatFace = null;
	String s = "Press on the screen to save your image as " +  trainingName + " in the database.";
    
	 
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
				
		  switch (event.getAction()) {
		    case MotionEvent.ACTION_DOWN:
		 	    
		    	if(MatFace != null)
		    	{
		    		String testImageName = null;
		    		String[] fileNames;
		    		
		    		ArrayList<String> tem2p = new ArrayList<String>();
					String [] tempFiles = this.getFilesDir().list();
					for(int a = 0 ; a<= (tempFiles.length-1); a++)
					{
						if(tempFiles[a].endsWith("png")){
							tem2p.add(tempFiles[a]);
						}
					}
					fileNames = new String[tem2p.size()];
					
					for(int f = 0; f <= (tem2p.size()-1);f++){
						fileNames[f] = tem2p.get(f);
					}

		    		if(fileNames.length != 0){
		    		for(int c = 0; c<= (fileNames.length - 1);c++){
		    			if(fileNames[c].endsWith(".png")){						//new if code
		    			String FullName = fileNames[c].replace(".png","");
		    			String labelNumber = FullName.split("\\-")[0];			// use this for label
						String temp = FullName.split("\\-")[1];
						String rawName = temp.split("\\_")[0];					//use this for raw name
						String temp2 = temp.split("\\_")[1];
		    			
						String newName = trainingName;

						if(rawName.equals(newName))
						{	//THIS WORKS. Fixed the name skipping bug.
							
							int numOfFacesDetected = 0;			
							for(int r = 0; r<= (fileNames.length-1);r++)
							{
								String fullName = fileNames[r];
								if(fullName.contains(rawName))
								{
									numOfFacesDetected++;
								}
							}
							
							
							testImageName = labelNumber + "-" + newName + "_" + (numOfFacesDetected+1)+".png";
							
							break;
						}
						else
						{	//WORKING now
							if(c==(fileNames.length-1)){
							int numOfPeople = 0;
							String previousName = null; 
							for(int x = 0; x<= (fileNames.length-1);x++)
							{
								String fullName = fileNames[x];
								String tmp = fullName.split("\\-")[1];
								String rawNametmp = tmp.split("\\_")[0];					//use this for raw name
								
								if(!rawNametmp.equals(previousName))
								{
									numOfPeople++;
								}
								
								previousName = rawNametmp;
							}
							testImageName = (numOfPeople+1) + "-" + newName + "_" + 1 +".png";
							break;
						}
						}
						
		    		}}		//new if addition bracket here
		    		}else{
		    			testImageName = 1 + "-" + trainingName + "_" + 1 +".png";
		    		}
		    		System.out.println("zod new image name is " + testImageName);
		    		
		    		
		    		String filename = testImageName ;
		    		//File file = new File(path, filename);
		    		File file = new File(this.getFilesDir(), filename);
		    		
		    		Boolean bool = null;
		    		filename = file.toString();
		    		bool = opencv_highgui.imwrite(filename, MatFace);
		    		
		    		if (bool == true){
		    		    System.out.println("zod SUCCESS writing image to external storage");
		    		}
		    		  else{
		    		    System.out.println( "zod Fail writing image to external storage");
		    		  }
		    		
		    		
		    		
		    		s = "Image saved as " + testImageName;
		    	}
		      
		      return true;
		    case MotionEvent.ACTION_MOVE:
		   
		      break;
		    case MotionEvent.ACTION_UP:
		    
		      break;
		    default:
		      return false;
		    }
		return true;
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_recognition);

   	
		try {
			
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            layout = new FrameLayout(Training.this);
            trainView = new TrainView(Training.this);
            mPreview = new Preview(Training.this, trainView);
            layout.addView(mPreview);
            layout.addView(trainView);
            setContentView(layout);
        } catch (IOException e) {
            e.printStackTrace();
            new AlertDialog.Builder(Training.this).setMessage(e.getMessage()).create().show();
        }
		
	}
	

	
	
	class TrainView extends View implements Camera.PreviewCallback {
	    public static final int SUBSAMPLING_FACTOR = 4;			

	    public IplImage grayImage;					//was static in faceView
	    private CvHaarClassifierCascade classifier;
	    private CvMemStorage storage;
	    private CvSeq faces;
	    private Context context;

	    public TrainView(Training context) throws IOException {
	        super(context);
	        this.context = context;

	        boolean isCopyCompleted = CopyFile("haarcascade_frontalface_alt2.xml");
			Log.e("", "isCopyCompleted:::" + isCopyCompleted);


			File classifierFile = new File(context.getCacheDir(),
					"haarcascade_frontalface_alt2.xml");
			
			if (classifierFile == null || classifierFile.length() <= 0) {
				throw new IOException(
						"Could not extract the classifier file from Java resource.");
			}
			
	        // Preload the opencv_objdetect module to work around a known bug.
	        Loader.load(opencv_objdetect.class);
	        classifier = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
	        classifierFile.delete();
	        if (classifier.isNull()) {
	            throw new IOException("Could not load the classifier file.");
	        }
				storage = CvMemStorage.create();
	        
	        Log.d("Classifier Status", "Classifier found and successfully loaded.");
	        
	    }

	    //IMPORTANT THIS FUNCTION IS BEING CHANGED, PARAMETER CHANGED SO CHECK MASTER SERVER VERSION TO ROLL BACK
	    private boolean CopyFile(String cascadeName) {

			boolean isCopyCompleted = false;

			InputStream inputStream = null;
			OutputStream outputStream = null;

			try {
				inputStream = getResources().getAssets().open(
						cascadeName);
				File outFile = new File(context.getCacheDir(),
						cascadeName);
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
	       
	        
	        faces = cvHaarDetectObjects(grayImage, classifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
	        Log.d("Face View", "zod12/10 Cascade successfully used on gray images and the faces detected stored.");
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
			canvas.drawText(s, (getWidth() - textWidth) / 2, 20, paint);

			if (faces != null) {

				paint.setStrokeWidth(2);
				paint.setStyle(Paint.Style.STROKE);
				float scaleX = (float) getWidth() / grayImage.width();
				float scaleY = (float) getHeight() / grayImage.height();
				int total = faces.total();


				for (int i = 0; i < total; i++) {

					CvRect r = new CvRect(cvGetSeqElem(faces, i));
					int x = r.x(), y = r.y(), w = r.width(), h = r.height();

					IplImage testImage1 = grayImage.clone();

					cvSetImageROI(testImage1, cvRect(x - 15, y - 50, w + 35, h + 90));
					IplImage resizeImage = IplImage.create(140, 190, testImage1.depth(), testImage1.nChannels());
					opencv_imgproc.cvResize(testImage1, resizeImage, opencv_imgproc.CV_INTER_AREA);

					MatFace = new Mat(resizeImage);

					canvas.drawRect(x * scaleX, y * scaleY, (x + w) * scaleX, (y + h) * scaleY, paint);


				}
			}

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
	

