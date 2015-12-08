package com.zod.facedetectionjavaandroid;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class RealTimeRecognition extends Activity {

	 private FrameLayout layout;
	    private FaceView faceView;
	    private Preview mPreview;
	    
	    
	    
	    // We need the phone orientation to correctly draw the overlay:
	    private int mOrientation;
	    private int mOrientationCompensation;
	    private OrientationEventListener mOrientationEventListener;

	    // Let's keep track of the display rotation and orientation also:
	    private int mDisplayRotation;
	    private int mDisplayOrientation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//setContentView(R.layout.activity_real_time_recognition);
		
		  // Create our Preview view and set it as the content of our activity.
        try {
            layout = new FrameLayout(this);
            faceView = new FaceView(this);
            mPreview = new Preview(this, faceView);
            layout.addView(mPreview);
            layout.addView(faceView);
            setContentView(layout);
        } catch (IOException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this).setMessage(e.getMessage()).create().show();
        }
        
        mOrientationEventListener = new SimpleOrientationEventListener(this);
        mOrientationEventListener.enable();
	}
	
	@Override
	protected void onPause() {
		mOrientationEventListener.disable();
		super.onPause();
		
	}
	
	@Override
	protected void onResume() {
		mOrientationEventListener.enable();
		super.onResume();
		
	}
	
	private class SimpleOrientationEventListener extends OrientationEventListener {

        public SimpleOrientationEventListener(Context context) {
            super(context, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            // We keep the last known orientation. So if the user first orient
            // the camera then point the camera to floor or sky, we still have
            // the correct orientation.
            if (orientation == ORIENTATION_UNKNOWN) return;
            mOrientation = Util.roundOrientation(orientation, mOrientation);
            // When the screen is unlocked, display rotation may change. Always
            // calculate the up-to-date orientationCompensation.
            int orientationCompensation = mOrientation
                    + Util.getDisplayRotation(RealTimeRecognition.this);
            if (mOrientationCompensation != orientationCompensation) {
                mOrientationCompensation = orientationCompensation;
                faceView.setOrientation(mOrientationCompensation);
            }
        }
    }
	
	
}
