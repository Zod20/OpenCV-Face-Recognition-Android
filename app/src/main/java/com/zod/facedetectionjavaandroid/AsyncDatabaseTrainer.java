package com.zod.facedetectionjavaandroid;

import static org.bytedeco.javacpp.opencv_contrib.*;
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvRect;
import static org.bytedeco.javacpp.opencv_core.cvSetImageROI;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_highgui;

import android.os.AsyncTask;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AsyncDatabaseTrainer extends AsyncTask<String, String, String> {
	
	AssetManager am ;
	public static String []  assetFiles = null;
	private Context context;
	
	
	public AsyncDatabaseTrainer(Context myContext)
	{
		this.context = myContext;
	}
	
	
	@Override
	protected String doInBackground(String... params) {
		publishProgress("Loading...");
	
		//am = context.getAssets();
	/*	
		try {
			assetFiles = context.getFilesDir().list();						//files names use as labels later on
			
			System.out.println("zod first pass done " + assetFiles.length);
			for(int f = 0 ; f <= (assetFiles.length-1); f++)
			{
				System.out.println("zod first pass " + assetFiles[f]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}*/
        
		try {
			//assetFiles = am.list("images");						//files names use as labels later on
			ArrayList<String> temp = new ArrayList<String>();
			String [] tempFiles = context.getFilesDir().list();
			for(int a = 0 ; a<= (tempFiles.length-1); a++)
			{
				if(tempFiles[a].endsWith("png")){
					temp.add(tempFiles[a]);
				}
			}
			
			for(int f = 0; f <= (temp.size()-1);f++){
				System.out.println("zod thw " + temp.get(f) );
			}
			assetFiles = new String[temp.size()];
			
			for(int f = 0; f <= (temp.size()-1);f++){
				assetFiles[f] = temp.get(f);
			}
			
			System.out.println("zod thw pass done");
			//assetFiles = context.getFilesDir().list();						//files names use as labels later on
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(assetFiles.length == 0)
		{
	
			try {
				
				String [] defaultFiles  = context.getAssets().list("");
				
				for( int z = 0 ; z<= (defaultFiles.length - 1) ; z ++ )
				{
					if(defaultFiles[z].endsWith(".png")){
						InputStream isr = context.getAssets().open(defaultFiles[z]);
					
						Mat img = readInputStreamIntoMat(isr);
					
						File file = new File(context.getFilesDir(), defaultFiles[z]);
			    		
			    		Boolean bool = null;
			    		String filename = file.toString();
			    		bool = opencv_highgui.imwrite(filename, img);	
			    		
					}
				}
				System.out.println("zod second pass done");
			} catch (Exception e) {
				System.out.println("zod the error faced was " + e.getMessage());
			}
			
		}
		
		try {
			//assetFiles = am.list("images");						//files names use as labels later on
			ArrayList<String> temp = new ArrayList<String>();
			String [] tempFiles = context.getFilesDir().list();
			for(int a = 0 ; a<= (tempFiles.length-1); a++)
			{
				if(tempFiles[a].endsWith("png")){
					temp.add(tempFiles[a]);
				}
			}
			assetFiles = new String[temp.size()];
			
			for(int f = 0; f <= (temp.size()-1);f++){
				assetFiles[f] = temp.get(f);
			}
			
			System.out.println("zod thw pass done");
			//assetFiles = context.getFilesDir().list();						//files names use as labels later on
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
			
		MatVector images = new MatVector(assetFiles.length );			//this had -2 to length as well
		
		Mat labels = new Mat(assetFiles.length ,1,CV_32SC1);			//this had -2 to length
		IntBuffer labelsBuf = labels.getIntBuffer();
		
		int counter;
		MainActivity.correspondingNames.clear();
		

		for(counter = 0; counter<= (assetFiles.length - 1) ; counter++)		
		{
			
			if(assetFiles[counter].endsWith(".png")){		//new code
			Mat img = null;
			InputStream is;
			try {
				File file1 = new File(context.getFilesDir(), assetFiles[counter]);
				is = new FileInputStream(file1);
				//is = am.open( assetFiles[counter]);
				img = readInputStreamIntoMat(is);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
	        int label = Integer.parseInt(assetFiles[counter].split("\\-")[0]);
	        MainActivity.correspondingNames.add(assetFiles[counter]);
	        
            images.put(counter, img);
            
            labelsBuf.put(counter, label);
            
			}
           
		}

		System.out.println("zod new bug " + "Starting to load algorithm");
		
		//TODO constants not loading from the settingmain1 class. run some tests to get the bug
		if(MainActivity.algorithm.equals("LBP"))
		{
			MainActivity.faceRecognizer = createLBPHFaceRecognizer(SettingMain1.LBPradius, SettingMain1.LBPneighbours
					, SettingMain1.LBPgridX, SettingMain1.LBPgridY, SettingMain1.LBPThreshold);		//LBP ALGORITHM
			
		}
		else if(MainActivity.algorithm.equals("Fisher"))
		{
			MainActivity.algorithm = "Eigen";
			int FisherThreshold = 3500;
			int FisherComponents = 0;
			String sentence  = readMemory("Fisher");
			if(sentence!=null){
				FisherThreshold = Integer.parseInt(sentence.split("%")[1]);
				FisherComponents = Integer.parseInt(sentence.split("%")[0]);
			}
			MainActivity.faceRecognizer = createEigenFaceRecognizer(FisherComponents,
					FisherThreshold);
			System.out.println("zod current is hola " + FisherThreshold + " comp " + FisherComponents);
		}
		else if(MainActivity.algorithm.equals("Eigen")){
			MainActivity.algorithm = "Eigen";
			
			int EigenThreshold = 2000;	//default values
			int EigenComponents = 80;
			String sentence  = readMemory("Eigen");
			if(sentence!=null){
				EigenThreshold = Integer.parseInt(sentence.split("%")[1]);
				EigenComponents = Integer.parseInt(sentence.split("%")[0]);
			}
			MainActivity.faceRecognizer = createEigenFaceRecognizer
					(EigenComponents,EigenThreshold);	//EIGENFACE ALGORITHM
			MainActivity.algorithm = "Eigen";
			System.out.println("zod current is hola " + EigenThreshold + " comp " + EigenComponents);
			
		}else if(MainActivity.algorithm.equals("Default")){
			MainActivity.algorithm = "Eigen";
			
			int EigenThreshold = 2000;	//default values
			int EigenComponents = 80;
			String sentence  = readMemory("Eigen");
			if(sentence!=null){
				EigenThreshold = Integer.parseInt(sentence.split("%")[1]);
				EigenComponents = Integer.parseInt(sentence.split("%")[0]);
			}
			MainActivity.faceRecognizer = createEigenFaceRecognizer
					(EigenComponents,EigenThreshold);	//EIGENFACE ALGORITHM
			MainActivity.algorithm = "Eigen";
			System.out.println("zod current is hola " + EigenThreshold + " comp " + EigenComponents);
			
		}
		
		MainActivity.faceRecognizer.train(images, labels);
		
		
		String resp = "Recognition Engine loaded successfully. Number of images: " + counter;
		MainActivity.trainingDone = true;

		return resp;
	}
	
	
	@Override
	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);
		MainActivity.status = values[0];
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		MainActivity.status = result;
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
	
	
	private String readMemory(String alg)
	 {
		 try{
		 File file = new File(context.getFilesDir().getAbsolutePath(), alg+".txt");
		 BufferedReader br = new BufferedReader(new FileReader(file));
	     String line;
	     line = br.readLine();
	     br.close();
	     return line;
		 }
		 catch(Exception e)
		 {
			 System.out.println("zod error " + e.getMessage());
		 }
		 return null;
	 }
}
