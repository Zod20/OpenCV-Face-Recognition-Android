package com.zod.facedetectionjavaandroid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingMain1 extends Activity {
	
	private Context context;
	
	//First View main
	Button Reset;
	Button DeleteDatabase;
	Button Eigen;
	Button LBP;
	Button Fisher;
	
	//Eigen view
	Button eigenSave;
	Button fisherSave;
	Button lbpSave;
		
	//Edit text
	TextView algorithmStatus;
	EditText LBPThresholde;
	EditText LBPradiusE;
	EditText LBPgridXe;
	EditText LBPgridYe;
	EditText LBPneighboursE;
	EditText FisherThresholdE;
	EditText FisherComponentsE;
	EditText EigenThresholdE;
	EditText EigenComponentsE;
	
	//variables for settings
	 public static int EigenThreshold = 2000;	//default values
	 public static int EigenComponents = 80;
	 public static int FisherThreshold = 3500;
	 public static int FisherComponents = 0;
	 public static int LBPThreshold = 100;
	 public static int LBPradius = 1;
	 public static int LBPgridX = 8;
	 public static int LBPgridY = 8;
	 public static int LBPneighbours  = 8;
	
	 
	 
	 
	 public String readMemory(String alg)
	 {
		 try{
		 File file = new File(SettingMain1.this.getFilesDir().getAbsolutePath(), alg+".txt");
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
	 
	 public void saveSettings(String alg){
		 try{
		 File file = new File(SettingMain1.this.getFilesDir().getAbsolutePath(), alg+".txt");
		 FileWriter writer = new FileWriter(file);
		 
		 if(alg.equals("LBP")){
			 writer.append(LBPradius + "%" + LBPneighbours + "%" + LBPgridX + "%" + LBPgridY + "%" + LBPThreshold);
		 }else if(alg.equals("Eigen"))
		 {
			 writer.append( EigenComponents + "%" + EigenThreshold);
		 }else if(alg.equals("Fisher"))
		 {
			 writer.append( FisherComponents + "%" + FisherThreshold);
		 }     
	     writer.flush();
	     writer.close();	
		 }
		 catch(Exception e)
		 {
			 System.out.println("zod didnt work " + e.getMessage());
		 }
	 }
	 
	
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_main1);
		
		context = this.context;
		
		
		algorithmStatus = (TextView)findViewById(R.id.ChooseAlgorithm);
		algorithmStatus.setText("Choose the recognition algorithm. Current algorithm - " + MainActivity.algorithm);
		
		
		Reset = (Button)findViewById(R.id.Reset);
		Reset.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 EigenThreshold = 2000;	//default values
				 EigenComponents = 80;
				 FisherThreshold = 3500;
				 FisherComponents = 0;
				 LBPThreshold = 100;
				 LBPradius = 1;
				 LBPgridX = 8;
				 LBPgridY = 8;
				 LBPneighbours  = 8;
				
				 saveSettings("Eigen");
				 saveSettings("Fisher");
				 saveSettings("LBP");
				 MainActivity.algorithm = "Default";
				 
		         Toast.makeText(getApplicationContext(),"Settings have been successfully reset.", Toast.LENGTH_SHORT).show();

			}
		});
			
		LBP = (Button)findViewById(R.id.LBP);
		LBP.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setContentView(R.layout.activity_settings_lbp);
				
			
				//MainActivity.algorithm = "LBP";
				//eigen method of -1 seems to work with all 3 algorithms so using that instead here
				//MainActivity.algorithm = "Eigen";
				LBPThresholde  = (EditText)findViewById(R.id.LBPthreshold);
				LBPgridXe  = (EditText)findViewById(R.id.LBPgridX);
				LBPgridYe  = (EditText)findViewById(R.id.LBPgridY);
				LBPneighboursE  = (EditText)findViewById(R.id.LBPneighbours);
				LBPradiusE  = (EditText)findViewById(R.id.LBPradius);
				
				
				String sentence = readMemory("LBP");
				if(sentence!=null){
					LBPradiusE.setText(sentence.split("%")[0]);
					LBPneighboursE.setText(sentence.split("%")[1]);
					LBPgridXe.setText(sentence.split("%")[2]);
					LBPgridYe.setText(sentence.split("%")[3]);
					LBPThresholde.setText(sentence.split("%")[4]);
				}
				System.out.println("zod threshold " + LBPThresholde.getText().toString());
				System.out.println("zod radius " + LBPradiusE.getText().toString());
				
				
				lbpSave = (Button)findViewById(R.id.Save);
				lbpSave.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
						LBPThreshold = Integer.parseInt(LBPThresholde.getText().toString());
						LBPgridX = Integer.parseInt(LBPgridXe.getText().toString());
						LBPgridY = Integer.parseInt(LBPgridYe.getText().toString());
						LBPneighbours = Integer.parseInt(LBPneighboursE.getText().toString());
						LBPradius = Integer.parseInt(LBPradiusE.getText().toString());
						
						saveSettings("LBP");
						MainActivity.algorithm = "LBP";
		                Toast.makeText(getApplicationContext(),"Settings have been saved and the recognition" +
		                		" engine has been set to LBP.", Toast.LENGTH_SHORT).show();
		                
		                onBackPressed();
					}
				});
				
			}
		});
		
		
		Fisher = (Button)findViewById(R.id.Fisher);
		Fisher.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setContentView(R.layout.activity_settings_fisher);
				
				//MainActivity.algorithm = "Fisher";
				//eigen method of -1 seems to work with all 3 algorithms so using that instead here
				//MainActivity.algorithm = "Eigen";
				FisherThresholdE = (EditText)findViewById(R.id.FisherThreshold);
				FisherComponentsE = (EditText)findViewById(R.id.FisherComponents);
				
				String sentence  = readMemory("Fisher");
				if(sentence!=null){
					FisherThresholdE.setText(sentence.split("%")[1]);
					FisherComponentsE.setText(sentence.split("%")[0]);
				}

				fisherSave = (Button)findViewById(R.id.Save);
				fisherSave.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						FisherThreshold = Integer.parseInt(FisherThresholdE.getText().toString());
						FisherComponents = Integer.parseInt(FisherComponentsE.getText().toString());
						
						saveSettings("Fisher");
						MainActivity.algorithm = "Fisher";
		                Toast.makeText(getApplicationContext(),"Settings have been saved and the recognition" +
		                		" engine has been set to Fisher.", Toast.LENGTH_SHORT).show();
		                
		                onBackPressed();
					}
				});
				
			}
		});
		
		
		
		Eigen = (Button)findViewById(R.id.Eigen);
		Eigen.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setContentView(R.layout.activity_settings_eigen);
			
				//MainActivity.algorithm = "Eigen";	
				EigenThresholdE = (EditText)findViewById(R.id.EigenThreshold);
				EigenComponentsE = (EditText)findViewById(R.id.EigenComponents);
				
				String sentence  = readMemory("Eigen");
				if(sentence!=null){
					EigenThresholdE.setText(sentence.split("%")[1]);
					EigenComponentsE.setText(sentence.split("%")[0]);
				}
				
				eigenSave = (Button)findViewById(R.id.Save);
				eigenSave.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						EigenThreshold = Integer.parseInt(EigenThresholdE.getText().toString());
						EigenComponents = Integer.parseInt(EigenComponentsE.getText().toString());
						
						saveSettings("Eigen");
						MainActivity.algorithm = "Eigen";
		                Toast.makeText(getApplicationContext(),"Settings have been saved and the recognition" +
		                		" has been set to Eigen recogniser.", Toast.LENGTH_SHORT).show();
		                
		                onBackPressed();
					}
				});
				
			}
		});
		
		/*
		Reset = (Button)findViewById(R.id.Reset);
		Reset.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				EigenThreshold = 2000;	//default values
				EigenComponents = 80;
				FisherThreshold = 3500;
				FisherComponents = 0;
				LBPThreshold = 100;
				LBPradius = 1;
				LBPgridX = 8;
				LBPgridY = 8;
				LBPneighbours  = 8;
				
				saveSettings("LBP");
				saveSettings("Fisher");
				saveSettings("Eigen");
				
                Toast.makeText(getApplicationContext(),"Recognition settings set to default.", Toast.LENGTH_SHORT).show();

			}
		});*/
		
		DeleteDatabase = (Button)findViewById(R.id.DeleteDatabaseButton);
		DeleteDatabase.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//TODO later use alert dialog to confirm delete. check bookmarks for good example
				//--------------use this to empty the directory database------------------
	    		String[] fileNames = getApplicationContext().getFilesDir().list();
	    		for(int t = 0; t<=(fileNames.length-1);t++)
	    		{			
	    			if(fileNames[t].endsWith("png")){
	    				File file1 = new File(getApplicationContext().getFilesDir(), fileNames[t]);
	    				file1.delete();
	    			}
	    		}
	    		//--------------------------------------------------------------------------
                Toast.makeText(getApplicationContext(),"Database has been deleted.", Toast.LENGTH_SHORT).show();

				
			}
		});
		
		
	}
}
