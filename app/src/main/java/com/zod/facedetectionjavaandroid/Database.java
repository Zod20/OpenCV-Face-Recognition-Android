package com.zod.facedetectionjavaandroid;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class Database extends Activity {
	
	private GridView gridView;
    private GridViewAdapter gridAdapter;


	@Override
	protected void onRestart() {
		super.onRestart();

		//done so that after going back from details activity the images refresh
		gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, getData());
		gridView.setAdapter(gridAdapter);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_database);
		
		gridView = (GridView) findViewById(R.id.gridView);
		
		
        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, getData());
        gridView.setAdapter(gridAdapter);
        
        
        gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				//TODO this is not working yet
			/*	ImageItem item = (ImageItem) parent.getItemAtPosition(position);
				System.out.println("zod imageitem retrieved");
				//Create intent
				Intent intent = new Intent(Database.this, DetailsActivity.class);
				System.out.println("zod details acitvity intent declared");
				intent.putExtra("title", item.getTitle());
				System.out.println("zod title put into intent");
				intent.putExtra("image", item.getImage());
				System.out.println("zod image put into intent");

				//Start details activity
				startActivity(intent);
				System.out.println("zod actual acitivty started");
				*/
				ImageItem item = (ImageItem) parent.getItemAtPosition(position);
				//Create intent
				System.out.println("zod1 item got");
				Intent intent = new Intent(Database.this, DetailsActivity.class);
				System.out.println("zod1 details acitvity intent declared");
				intent.putExtra("title", item.getTitle());
				intent.putExtra("image", item.getImage());
				System.out.println("zod1 intent stuffed");
				//Start details activity
				startActivity(intent);
				System.out.println("zod1 activity started");
			}
		});
	}
	
	

	private ArrayList<ImageItem> getData() {
        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        
        
        //TODO load the image database here fom internal memory
        
        try{
	        String[] filesList = this.getFilesDir().list();
	        for( int i = 0 ; i<= (filesList.length -1) ; i++ )
	        {
	        	if(filesList[i].endsWith(".png")){	//new code
		        	Bitmap bitmap = BitmapFactory.decodeStream(this.openFileInput(filesList[i]));
					imageItems.add(new ImageItem(bitmap, filesList[i]));
	        	}
	        }
        }
        catch(Exception e)
        {
        	System.out.println("zod " + " couldnt do it boss " + e.getMessage() );
        }
    
        return imageItems;
    }
	
	
	
	
	
	public class ImageItem{
		private Bitmap image;
		private String title;

		public ImageItem(Bitmap image, String title) {
			super();
			this.image = image;
			this.title = title;
		}

		public Bitmap getImage() {
			return image;
		}

		public void setImage(Bitmap image) {
			this.image = image;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}
	}
}
