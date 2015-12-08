package com.zod.facedetectionjavaandroid;


import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import static org.bytedeco.javacpp.opencv_imgproc.warpAffine;
import static org.bytedeco.javacpp.opencv_imgproc.getRotationMatrix2D;
import static org.bytedeco.javacpp.opencv_core.cvFlip;
import static org.bytedeco.javacpp.opencv_core.cvGetSeqElem;
import static org.bytedeco.javacpp.opencv_core.cvLoad;
import static org.bytedeco.javacpp.opencv_core.cvRect;
import static org.bytedeco.javacpp.opencv_core.cvSetImageROI;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_highgui;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;
import org.bytedeco.javacpp.helper.opencv_imgproc;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DetailsActivity extends Activity {


	 public static String title;
	 public static ImageView imageView;
	public static ImageView croppedView;




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.details_activity);


			title = getIntent().getStringExtra("title");
			Bitmap bitmap = getIntent().getParcelableExtra("image");



			imageView = (ImageView) findViewById(R.id.image);

			imageView.setImageBitmap(bitmap);

			croppedView = (ImageView) findViewById(R.id.croppedImage);
			croppedView.setImageBitmap(bitmap);

			AsyncTaskRunner runner1 = new AsyncTaskRunner(this);
			runner1.execute("Display");


		}catch(Exception e)
		{
			System.out.println("zod1 problem met " + e.getMessage());
		}
	}
	
	
	public static class AsyncTaskRunner extends AsyncTask<String, String, String>{

		String resp;
		private Context context;
		public static final int SUBSAMPLING_FACTOR = 4;			
	    public IplImage grayImage;					//was static in faceView
	    private CvHaarClassifierCascade classifier;
	    private CvMemStorage storage;
	    private CvSeq faces;
	    private Bitmap bit;
		private Bitmap croppedBit;
	    private Canvas c;
		private Canvas c1;
		public boolean hasRotated = false;
	    
	    int x = 0 ;
	    int y = 0;
	    int w = 0;
	    int h = 0;
		int xCenter = 0;
		int yCenter = 0;
	    
	    int x2 = 0 ;
	    int y2 = 0;
	    int w2 = 0;
	    int h2  = 0;
		int x2Center = 0;
		int y2Center = 0;

		float _d = 0;
		float _1point6d = 0;
		float _1point8d = 0;
		float _0point9d = 0;
		
		public AsyncTaskRunner(Context myContext)
		{
			this.context = myContext;
		}
		
		@Override
		protected String doInBackground(String... params) {
			publishProgress("Sending...");
			String msg = params[0];
			try
			{
				boolean isCopyCompleted = CopyFile();
				Log.e("", "isCopyCompleted:::" + isCopyCompleted);


				File classifierFile = new File(context.getCacheDir(),
						"haarcascade_eye.xml");
				
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
				System.out.println("zod123 changing the value");
				
				//TODO load the image from memory and then apply the classifier on it
				File bitmapFile = new File(context.getFilesDir(), title);
				FileInputStream fis = new FileInputStream(bitmapFile);
				Mat img = readInputStreamIntoMat(fis);






				cvClearMemStorage(storage);
				faces = cvHaarDetectObjects(img.asIplImage(), classifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
				
				System.out.println("zod123 it worked " + faces.total());
				int total = faces.total();
				 for (int i = 0; i < total; i++)
		          {
					 CvRect r = new CvRect(cvGetSeqElem(faces, i));   
					 
					 
					 if(x==0){
						 x = r.x() ;
						 y = r.y();
						 w = r.width();
						 h = r.height();
						 xCenter = (x + (x+w))/2;
						 yCenter = (y + (y+h))/2;
					 }else if(x2==0){
						 x2 = r.x() ;
						 y2 = r.y();
						 w2 = r.width();
						 h2 = r.height();
						 x2Center = (x2 + (x2+w2))/2;
						 y2Center = (y2 + (y2+h2))/2;
					 }
		             
		             System.out.println("zod123 number " + (i+1) + " - x = " + x + " y = " + y );
		             
		             
		             Bitmap temPbit = BitmapFactory.decodeFile(new File(context.getFilesDir(),title).getAbsolutePath());
					 bit = temPbit.copy(Bitmap.Config.ARGB_8888, true);

				    
				     	     
				     
		          }





				//6/10 new stuff experimental ---------------------

				//create a separate cropped mat image
				IplImage testImage1 = img.asIplImage().clone();


					if (x >= x2) {
						cvSetImageROI(testImage1, cvRect(x2, 0, x + (w / 2), 100));
						_d = xCenter - x2Center;
					}
					else {
						cvSetImageROI(testImage1, cvRect(x, 0, x2 + (w2 / 2), 100));
						_d = x2Center - xCenter;

					}

				System.out.println("zod7/10 x,y center coordniates : " + xCenter + "  ,  " + yCenter);
				System.out.println("zod7/10 x2,y2 center coordniates : " + x2Center + "  ,  " + y2Center);

				 _0point9d = 0.9f*_d;
				_1point6d = (1.6f-0.1f)*_d;
				_1point8d = (1.8f + 0.2f)*_d;

				System.out.println("zod7/10 d = " + _d + " , 0.9d = " + _0point9d + " ,  1.6fd = "  + _1point6d
				+ " , 1.8d  =  " + _1point8d);

				//^6/10 new stuff above experimental -----------------------

				//TODO use the x,y and x2,y2 to calculate the angle by which it has to rotate and then apply it
				//TODO save the mat and then later load this mat seperately from memory into croppedBit.
				//-------experimental code 10/10
				hasRotated = false;
				if(yCenter != y2Center)
				{
					/**/double degrees = 0;
					if(yCenter > y2Center)
					{
						degrees =-( Math.toDegrees(Math.atan( (yCenter - y2Center)/_d )));
					}else if( yCenter < y2Center)
					{
						degrees = (Math.toDegrees(Math.atan( (y2Center - yCenter)/_d )));
					}

					opencv_core.Point2f center = new opencv_core.Point2f(img.cols()/2, img.rows()/2);
					Mat rotImage = getRotationMatrix2D(center, degrees, 1.0);
					Mat dummy = new Mat();
					warpAffine(img, dummy, rotImage, img.size());
					Mat rotatedImage = dummy;

					File file = new File(context.getFilesDir(), "3-temp_3.png");

					Boolean bool = null;
					String filename = file.toString();
					bool = opencv_highgui.imwrite(filename, rotatedImage);
					hasRotated = true;

				}

				//--------experimental code 9/10

				
			}
			catch(Exception e)
			{
				
			}
			resp = "Message has been sent.";
			return resp;
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			//status.setText(values[0]);
			
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			try{
				if(hasRotated == false) {
					croppedBit = bit.copy(bit.getConfig(), true);
				}else{
					try {
						Bitmap temPbit = BitmapFactory.decodeFile(new File(context.getFilesDir(), "3-temp_3.png").getAbsolutePath());
						croppedBit = temPbit.copy(Bitmap.Config.ARGB_8888, true);
					}catch(Exception e)
					{
						System.out.println("zodLog " + e.getMessage());
						croppedBit = bit.copy(bit.getConfig(), true);
					}
				}

			Paint p = new Paint();
			 p.setColor(Color.RED);
				p.setTextSize(20);
			 p.setStrokeWidth(3);
	         p.setStyle(Paint.Style.STROKE);
	         
	         c = new Canvas(bit);


				//c.drawRect(x, y, x + w,
				//		y + h, p);
				//c.drawPoint(xCenter + 2, yCenter, p);
				//c.drawRect(x2, y2, x2+w2,
		        //        y2+h2, p);

				//c.drawPoint(x2Center + 2, y2Center, p);
				c.drawCircle(xCenter + 2, yCenter, 3,  p);
				c.drawCircle(x2Center + 2, y2Center, 3,  p);


			//DetailsActivity.titleTextView.setText("Eyes Detected!");

			DetailsActivity.imageView.setImageBitmap(bit);
			
			DetailsActivity.imageView.draw(c);


				//second cropped view overlaying canvas, used to simulate crop
				Paint p1 = new Paint();
				p1.setColor(Color.WHITE);
				p1.setStrokeWidth(-1);
				p1.setStyle(Paint.Style.STROKE);
				c1 = new Canvas(croppedBit);

				//TODO later do a if block that checks y and y2 to see if they are
				//different then takes measures accordingly
				for(int topcounter = 0; topcounter <= (int)(y - _1point6d) ; topcounter ++) {
					c1.drawLine(0, topcounter, 140, topcounter, p1);
				}
				for(int botcounter = (int)(y+_1point8d); botcounter <= 190 ; botcounter ++ )
				{
					c1.drawLine(0, botcounter, 140, botcounter, p1);
				}

				for(int leftcounter = 0; leftcounter <= ( (140 - (_d + 2 * _0point9d))/2); leftcounter ++)
				{
					c1.drawLine(leftcounter, 0, leftcounter, 190, p1);
				}

				for(int rightcounter = (int)( _d + 2 * _0point9d + ((140 - (_d + 2 * _0point9d))/2)); rightcounter <= 140; rightcounter ++)
				{
					c1.drawLine(rightcounter, 0, rightcounter, 190, p1);
				}

				DetailsActivity.croppedView.setImageBitmap(croppedBit);
				DetailsActivity.croppedView.draw(c1);



			}
			catch(Exception e)
			{
				System.out.println("zod123 error " + e.getMessage());
			}

			if(hasRotated == false) {
				//Toast.makeText(context, "Image cropped according to specification.", Toast.LENGTH_SHORT).show();
			}else{
				//Toast.makeText(context, "Image rotated and then cropped according to specification.", Toast.LENGTH_SHORT).show();
			}
			x=0;
			y=0;
			w=0;
			h=0;
			
			x2=0;
			y2=0;
			w2=0;
			h2=0;
			
			/*
			Matrix mat = new Matrix();
			mat.postRotate(2);
			Bitmap bMapRotate = Bitmap.createBitmap(bit, 0, 0,
			                             bit.getWidth(), bit.getHeight(), mat, true);
			BitmapDrawable bmd = new BitmapDrawable(bMapRotate);
			DetailsActivity.imageView.setImageBitmap(bMapRotate);
			DetailsActivity.imageView.setImageDrawable(bmd);
			*/
			
			//set the image here
	
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		private static Mat readInputStreamIntoMat(InputStream inputStream) throws IOException {
		    // Read into byte-array
		    byte[] temporaryImageInMemory = readStream(inputStream);

		    // Decode into mat. Use any IMREAD_ option that describes your image appropriately
		    Mat outputImage = opencv_highgui.imdecode(new Mat(temporaryImageInMemory), opencv_highgui.IMREAD_GRAYSCALE);

		    return outputImage;
		}

		private static byte[] readStream(InputStream stream) throws IOException {
		    // Copy content of the image to byte-array
		    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		    int nRead;
		    byte[] data = new byte[16384];

		    while ((nRead = stream.read(data, 0, data.length)) != -1) {
		        buffer.write(data, 0, nRead);
		    }

		    buffer.flush();
		    byte[] temporaryImageInMemory = buffer.toByteArray();
		    buffer.close();
		    stream.close();
		    return temporaryImageInMemory;
	
		}
		
		 private boolean CopyFile() {

				boolean isCopyCompleted = false;

				InputStream inputStream = null;
				OutputStream outputStream = null;

				try {
					inputStream = context.getAssets().open(
							"haarcascade_eye.xml");
					File outFile = new File(context.getCacheDir(),
							"haarcascade_eye.xml");
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
}
