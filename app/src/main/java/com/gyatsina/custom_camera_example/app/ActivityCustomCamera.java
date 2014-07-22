package com.gyatsina.custom_camera_example.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.util.Pair;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//import net.fotovio.fotovio.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//import fotovio.core.recorder.RecorderParameters;

public class ActivityCustomCamera extends Activity implements
		SurfaceHolder.Callback, Camera.AutoFocusCallback, Camera.PictureCallback {

	public static boolean isRecording;
	public static boolean resolutionChangeable;
    private static String photoFileName = "photo";
    private File photoFile;
    private String photoDirectoryPath;
    boolean mPreviewRunning = false;

	View thumbnailBox;
	View thumbnail;

	// Camera
	private Camera camera;
	private int currentCameraId = 0;

	// Recorder Parameters
//	private RecorderParameters recorderParameters;

	// CameraPreview
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private Handler autoFocusHandler;

	// MediaRecorder
	private int orientation = 0;
	private boolean recordClicked;
	private String videoDirectoryPath;
	private String photoFilePath = "";

	// recorded file
	private File videoFile;
	private int videoCounter = 0;

	// Timer
	private CountDownTimer countDownTimerMilliSeconds;

	private int maxDurationMilliSeconds = 0;
	private int maxDurationSeconds = 0;
	private int maxDurationMinutes = 0;

	private long remainingMilliSeconds = 0;
	private long remainingSeconds = 0;
	private long remainingMinutes = 0;

	private long currentVideoDuration = 0;

	// Views
	private Button btRecord;
	private LinearLayout timerLayout;
	private TextView txTimer;
	private ImageView imgSettings;

	// inflation of views
	private LayoutInflater inflater;

	private OrientationEventListener orientationEventListener;
	private boolean orientListenerEnabled = true;

	boolean lastVideoToDelete = false;
	boolean settingsAreVisible = false;

	SimpleDateFormat sDateFormat;
	String formatedDate;

	private long maxFileSize = 250000000;
	private int quality = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_recorder);

        Bundle extras = getIntent().getExtras();

        // initialize layout objects
        inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // current date
        sDateFormat = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss_ms");

        photoDirectoryPath = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/myCameraExample/images/";
        createDirIfNotExist(photoDirectoryPath);

        surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        btRecord = (Button) findViewById(R.id.camera_record);
        timerLayout = (LinearLayout) findViewById(R.id.camera_timer_layout);
        txTimer = (TextView) findViewById(R.id.camera_timer);
        imgSettings = (ImageView) findViewById(R.id.img_settings);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        StatFs stats = new StatFs(photoDirectoryPath);
        long availableBlocks = stats.getAvailableBlocks();
        long blockSizeInBytes = stats.getBlockSize();
        long freeSpaceInBytes = availableBlocks * blockSizeInBytes;

        // if Free space is less than 2 MB
        if (freeSpaceInBytes < 2000000) {
            Toast.makeText(ActivityCustomCamera.this,
                    getResources().getString(R.string.recorder_error_no_space),
                    1).show();
            finish();
        } else if (freeSpaceInBytes < maxFileSize) {
            maxFileSize = freeSpaceInBytes;
        }

//		orientationEventListener = new OrientationEventListener(this,
//				SensorManager.SENSOR_DELAY_NORMAL) {
//
//			@Override
//			public void onOrientationChanged(int arg0) {
//				orientation = arg0;
//			}
//		};

//		if (orientationEventListener.canDetectOrientation()) {
//			orientationEventListener.enable();
//			orientListenerEnabled = true;
//		} else {
//			orientationEventListener.disable();
//			orientListenerEnabled = false;
//		}

//		videoDirectoryPath = Environment.getExternalStorageDirectory()
//				.getAbsolutePath() + "/fotovio/videos/";
//
//		createDirIfNotExist(videoDirectoryPath);


        btRecord.setOnClickListener(new View.OnClickListener() {
            // @Override
            public void onClick(View v) {
                takePhotoPicture();
            }
        });
    }


    public void onAutoFocus(boolean success, Camera camera)
    {
//        camera.takePicture(null, null, this);
    }

    public void onPictureTaken(byte[] data, Camera camera)
    {
        formatedDate = sDateFormat.format(new Date(System
                .currentTimeMillis()));
        photoFilePath = photoDirectoryPath + "Photo_" + formatedDate
                + ".jpg";
        try
        {
//            FileOutputStream fos = openFileOutput(photoFileName, MODE_PRIVATE);
            FileOutputStream fos = new FileOutputStream(String.format(photoFilePath));
            fos.write(data);
            fos.close();
            Intent mainIntent = new Intent(ActivityCustomCamera.this, MainActivity.class);
            startActivity(mainIntent);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        Intent resultIntent = new Intent();
//        resultIntent.putExtra("photoFilePath", photoFileName);
        resultIntent.putExtra("photoFilePath", photoFilePath);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }



	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mPreviewRunning)
        {
            camera.stopPreview();
        }
		try {
			camera.setPreviewDisplay(holder);
			// prPreviewRunning = true;
        } catch (IOException IOe) {
            IOe.printStackTrace();
        }
        camera.startPreview();
        mPreviewRunning = true;
    }

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		getCamera();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getCamera();
	}

	private void getCamera() {
		if (camera == null && Camera.getNumberOfCameras() > 0) {
			camera = Camera.open(currentCameraId);

			Camera.Parameters params = camera.getParameters();
            params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            params.setPictureFormat(PixelFormat.JPEG);
            params.setPreviewSize(1280, 720);

            List<Size> sizes = params.getSupportedPictureSizes();
            Camera.Size size = sizes.get(0);
            for(int i=0;i<sizes.size();i++)
            {
                if(sizes.get(i).width > size.width)
                    size = sizes.get(i);
            }
            params.setPictureSize(size.width, size.height);
            camera.setParameters(params);

//			adjustPreviewSize(camera);

		}
		if (camera == null) {
			Toast.makeText(this.getApplicationContext(),
					"Camera is not available!", Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

        if (mPreviewRunning){
            camera.stopPreview();
        }
		if (camera != null) {
			camera.release();
			camera = null;
		}
	}

	@Override
	public void onBackPressed() {
		if (photoFileName != null) {
            System.out.println("--------------ActivityCustomCamera.onBackPressed !! add method to delete file!");
//			File file = new File(videoFilePath);
//			if (file.exists()) {
//				file.delete();
//			}
//			file = null;
		}
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		if (camera != null) {
			camera.release();
			camera = null;
		}
        super.onDestroy();
	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_CAMERA)
        {
            camera.autoFocus(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
        }
        mPreviewRunning = false;
//        camera = null;
	}


    private void takePhotoPicture() {
        camera.takePicture(null, null, this);
    }


	private boolean startRecording() {

//		quality = dbHelper.getCurrentQuality();
//
//		btRecord.setEnabled(false);
//		timerLayout.setVisibility(View.VISIBLE);
//		imgSettings.setVisibility(View.GONE);
//
//		// timelineLayoutFull.setVisibility(View.VISIBLE);
//		// imgEndRecording.setVisibility(View.VISIBLE);
//
//		if (quality != 0) {
//			// Samsung Galaxy hack for HD video
//			camera.getParameters().set("cam_mode", 1);
//		}
//		videoCounter++;
//		camera.stopPreview();
//		try {
//			camera.unlock();
//			mediaRecorder.setCamera(camera);
//
//			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
//			mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//
//			CamcorderProfile profile;
//			switch (quality) {
//			case 0:
//				profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
//				break;
//			case 1:
//				profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
//				break;
//			case 2:
//				profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
//				break;
//			default:
//				profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
//				break;
//			}
//
//			Pair<Integer, Integer> resolution = recorderParameters
//					.getResolution(quality);
//
//			// do NOT set these values, otherwise the hi-res video will not be
//			// in HD!!!
//			// profile.fileFormat = OutputFormat.DEFAULT;
//			// profile.audioCodec = AudioEncoder.DEFAULT;
//			// profile.videoCodec = VideoEncoder.DEFAULT;
//
//			profile.videoFrameWidth = resolution.first;
//			profile.videoFrameHeight = resolution.second;
//
//			if (recorderParameters.getBitRate(quality) != 0) {
//				profile.videoBitRate = recorderParameters.getBitRate(quality);
//			}
//
//			if (recorderParameters.getFrameRate(quality) != 0) {
//				profile.videoFrameRate = recorderParameters
//						.getFrameRate(quality);
//			}
//
//			mediaRecorder.setProfile(profile);
//
//			formatedDate = sDateFormat.format(new Date(System
//					.currentTimeMillis()));
//
//			// Video path, always .mp4!
//			videoFilePath = videoDirectoryPath + "Video_" + formatedDate
//					+ ".mp4";
//
//			videoFile = new File(videoFilePath);
//			mediaRecorder.setOutputFile(videoFile.getPath());
//
//			mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
//			mediaRecorder.setMaxDuration(maxDurationMilliSeconds);
//			mediaRecorder.setMaxFileSize(maxFileSize);
//
//			setOrientationOfMediaRecorder(mediaRecorder);
//
//			// prepare for capturing
//			// state: DataSourceConfigured => prepared
//			mediaRecorder.prepare();
//			// start recording
//			// state: prepared => recording
//			mediaRecorder.start();
//
//			countDownTimerMilliSeconds.start();
//			System.out.println("remaining milli seconds: "
//					+ remainingMilliSeconds);
//			isRecording = true;
//			resolutionChangeable = false;
//
//			// let the record button be disabled for 2 minutes
//			new Thread(new Runnable() {
//				public void run() {
//					try {
//						Thread.sleep(1000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					runOnUiThread(new Runnable() {
//
//						@Override
//						public void run() {
//							btRecord.setEnabled(true);
//							btRecord.setBackgroundDrawable(getResources()
//									.getDrawable(R.drawable.stop_button));
//						}
//					});
//				}
//			}).start();
//
//			return true;
//		} catch (IOException _le) {
//			mediaRecorder.release();
//			camera.release();
//			_le.printStackTrace();
			return false;
//		}
	}

	/**
	 * set the orientation of mediaRecorder to portrait or landscape
	 *
	 * @param mediaRecorder
	 */
	private void setOrientationOfMediaRecorder(MediaRecorder mediaRecorder) {
//		if (orientListenerEnabled) {
//			// if this is the first video, set the display orientation of the
//			// camera
//			if (videoCounter == 1) {
//				// portrait mode
//				if (orientation == 360 || orientation == 0
//						|| (orientation >= 0 && orientation < 45)
//						|| (orientation >= 135 && orientation < 180)
//						|| (orientation >= 180 && orientation < 215)
//						|| (orientation >= 315 && orientation < 360)) {
//					mediaRecorder.setOrientationHint(90);
//				}
//				// landscape mode
//				else if (orientation == 90 || orientation == 270
//						|| (orientation >= 45 && orientation < 90)
//						|| (orientation >= 90 && orientation < 135)
//						|| (orientation >= 215 && orientation < 270)
//						|| (orientation >= 270 && orientation < 315)) {
//					mediaRecorder.setOrientationHint(0);
//				} else {
//					mediaRecorder.setOrientationHint(0);
//				}
//				orientationEventListener.disable();
//				orientListenerEnabled = false;
//			}
//		} else {
//			mediaRecorder.setOrientationHint(0);
//		}
	}

	private void setCameraDefaults() {
		Camera.Parameters params = camera.getParameters();

		// Supported picture formats (all devices should support JPEG).
		List<Integer> formats = params.getSupportedPictureFormats();

		if (formats.contains(ImageFormat.JPEG)) {
			params.setPictureFormat(ImageFormat.JPEG);
			params.setJpegQuality(100);
		} else
			params.setPictureFormat(PixelFormat.RGB_565);

		// Now the supported picture sizes.
		List<Size> sizes = params.getSupportedPictureSizes();
		Size size = sizes.get(sizes.size() - 1);
		params.setPictureSize(size.width, size.height);

		// Set the brightness to auto.
		params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);

		// Set the flash mode to auto.
		params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);

		// Set the scene mode to auto.
		params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);

		// Lastly set the focus to auto.
		params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

		camera.setParameters(params);
	}

	/**
	 * stop recording when pause button is pressed
	 */
	private void stopRecording() {

//		// cancel the timer
//		countDownTimerMilliSeconds.cancel();
//
//		if (mediaRecorder != null && isRecording) {
//			try {
//				mediaRecorder.stop();
//				mediaRecorder = null;
//			} catch (RuntimeException e) {
//				// ...
//			}
//		}
//
//		Intent resultIntent = new Intent();
//		resultIntent.putExtra("videoFilePath", videoFilePath);
//		// TODO Add extras or a data URI to this intent as appropriate.
//		setResult(Activity.RESULT_OK, resultIntent);
//		finish();
	}

	public void adjustPreviewSize(Camera camera) {

//		Display display = getWindowManager().getDefaultDisplay();
//		int displayWidth = display.getWidth();
//		int displayHeight = display.getHeight();
//		float displayRatio = (float) displayWidth / (float) displayHeight;
//
//		Size size = camera.getParameters().getPreviewSize();
//		int previewWidth = size.width;
//		int previewHeight = size.height;
//
//		float previewRatio = (float) previewWidth / (float) previewHeight;
//
//		int surfaceHeight = displayHeight;
//		int surfaceWidth = displayWidth;
//
//		if (previewRatio != displayRatio) {
//			surfaceHeight = displayHeight;
//			surfaceWidth = (int) ((float) surfaceHeight * previewRatio);
//		}
//
//		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
//				surfaceWidth, surfaceHeight);
//		params.gravity = Gravity.CENTER;
//		SurfaceView surfaceCamera = (SurfaceView) findViewById(R.id.surface_camera);
//
//		// apply
//		surfaceCamera.setLayoutParams(params);
	}

	private void createDirIfNotExist(String _path) {
		File lf = new File(_path);
		try {
			if (lf.exists()) {
				// directory already exists
			} else {
				if (lf.mkdirs()) {
					// Log.v(TAG, "createDirIfNotExist created " + _path);
				} else {
					// Log.v(TAG, "createDirIfNotExist failed to create " +
					// _path);
				}
			}
		} catch (Exception e) {
			// create directory failed
			// Log.v(TAG, "createDirIfNotExist failed to create " + _path);
		}
	}
}