package com.scouterengine.detectingsample;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import com.scouterengine.lib.SpecificObjectDetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {
	
	SpecificObjectDetector mImageDetector;
	HashMap<Integer, String> mImageName = new HashMap<Integer, String>();

	private Camera mCamera;
	private SurfaceHolder mSurfaceHolder;
	private Handler mHandler;
	
	private int mFrameWidth;
	private int mFrameHeight;
	
	public CameraPreview(Context context, Handler handler) {
		super(context);
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		mHandler = handler;
		
		mImageDetector = new SpecificObjectDetector();
		readTemplateImages();
	}


	public void surfaceChanged(SurfaceHolder _holder, int format, int width,
			int height) {
		if (mCamera != null) {
			Camera.Parameters params = mCamera.getParameters();
			List<Camera.Size> sizes = params.getSupportedPreviewSizes();
			
			mFrameWidth = width;
			mFrameHeight = height;	
			int pixelSize = 320*240;
			double minDiff = Double.MAX_VALUE;
			for (Camera.Size size : sizes) {
				if (Math.abs(size.height*size.width - pixelSize) < minDiff) {
					mFrameWidth = size.width;
					mFrameHeight = size.height;
					minDiff = Math.abs(size.height*size.width - pixelSize);
				}
			}
			
            List<String> FocusModes = params.getSupportedFocusModes();
            if (FocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
            {
            	params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }            
            
			params.setPreviewSize(mFrameWidth, mFrameHeight);
			mCamera.setParameters(params);
            mCamera.setDisplayOrientation(90);
			mCamera.startPreview();
		}
	}
	
	public void OnDetect() {
		mCamera.setOneShotPreviewCallback(mCallback);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open(0);
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private PreviewCallback mCallback = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			Message msg = new Message();
			int templateImageId = mImageDetector.detect(mFrameWidth, mFrameHeight, data);
			final String name = mImageName.get(templateImageId);
			ObjInfo oi = new ObjInfo();
			oi.name = name;
			oi.objId = templateImageId;		
			msg.obj = oi;
			mHandler.sendMessage(msg);	
		}
	};
	
	public void restartPreviewCallback(){
		this.requestLayout();
		this.invalidate();
		if (mCamera != null) {
			mCamera.startPreview();
		}
	}

	
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mCamera != null) {
			synchronized (this) {
				mCamera.stopPreview();
				mCamera.setPreviewCallback(null);
				mCamera.release();
				mCamera = null;
			}
		}
	}

	private void readTemplateImages()	 {	
		mImageName.put(-1, "none");
    	Field[] fields = R.raw.class.getFields();		
		int size = 320;

		for (Field field : fields) {
			try {
				String name = field.getName();
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeResource(getResources(), (Integer)field.get(name), options);
				int scaleW = options.outWidth / size+1;
				int scaleH = options.outHeight / size+1;
				options.inSampleSize = Math.max(scaleW, scaleH);
				options.inJustDecodeBounds = false;
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(), (Integer)field.get(name), options);
				int id = mImageDetector.registerTemplateImage(bitmap);
				mImageName.put(id, name);
				bitmap.recycle();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mImageDetector.createIndex();
	}	
}