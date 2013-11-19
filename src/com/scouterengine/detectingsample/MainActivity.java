package com.scouterengine.detectingsample;

import java.util.Date;

import com.scouterengine.lib.SpecificObjectDetector;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	private CameraPreview mCameraPreview;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        LinearLayout parent = (LinearLayout)findViewById(R.id.camera_preview_layout);
		mCameraPreview = new CameraPreview(this, new MainHandler());
		parent.addView(mCameraPreview);
		
		Button btn = (Button)findViewById(R.id.show_detect_id_btn);
        btn.setOnClickListener(new OnClickListener(){
        	@Override
    		public void onClick(View v) {
        		mCameraPreview.OnDetect();
        	}
    	});
    }

    
    private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			String detectedName = ((ObjInfo)msg.obj).name;
			TextView txtName = (TextView)findViewById(R.id.textView1);
			Date date = new Date();
			txtName.setText(detectedName + "("+date.getMinutes() +":" +date.getSeconds() +":"+(date.getTime()%1000)/10+")");
			mCameraPreview.restartPreviewCallback();
		}
	}
    
}
