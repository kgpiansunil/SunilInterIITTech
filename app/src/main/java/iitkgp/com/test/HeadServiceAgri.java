package iitkgp.com.test;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.CycleInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HeadServiceAgri extends Service implements TextToSpeech.OnInitListener {
	
	private WindowManager mWindowManager;
	private WindowManager mWindowManagerNext;
	private View mPointer;
	private View mNext;
	WindowManager.LayoutParams params;
	WindowManager.LayoutParams paramsNext;
	private TextToSpeech tts;
	private static boolean isPlayed  = false;
	static final String EXTRA_RESULT_CODE = "resultCode";
	static final String EXTRA_RESULT_INTENT = "resultIntent";
	static final int helpDelay= 10000;
	private int resultCode;
	private Intent resultData;
	
	private MediaProjectionManager mgr;
	private MediaProjection projection;
	private VirtualDisplay vdisplay;
	static final int VIRT_DISPLAY_FLAGS =
		DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY |
			DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
	
	private Handler handler;
	
	private int lastScreen = -1;
	HashMap<String, String> paramsTTS;
	HashMap<String, String> paramsTTSSpeak;
	
	private ObjectAnimator scaleDown;
	
	private int initialX, initialY;
	private float initialTouchX, initialTouchY;
	
	final Runnable runnableSpeak = new Runnable() {
		public void run() {
			Log.d("Runnable","Handler is working");
			scaleDown.start();
			tts.speak("Help ke liye pointer pe dabaaye", TextToSpeech.QUEUE_FLUSH, paramsTTSSpeak);
		}
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@TargetApi(Build.VERSION_CODES.O)
	@Override
	public void onCreate() {
		super.onCreate();
		isPlayed = false;
		tts = new TextToSpeech(getApplicationContext(), this);
		mPointer = LayoutInflater.from(this).inflate(R.layout.pointer, null);
		mNext = LayoutInflater.from(this).inflate(R.layout.next_screen, null);
		
		int LAYOUT_FLAG = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
			: WindowManager.LayoutParams.TYPE_PHONE;
		
		params = new WindowManager.LayoutParams(
			WindowManager.LayoutParams.WRAP_CONTENT,
			WindowManager.LayoutParams.WRAP_CONTENT,
			LAYOUT_FLAG,
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
			PixelFormat.TRANSLUCENT);
		paramsNext = new WindowManager.LayoutParams(
			WindowManager.LayoutParams.WRAP_CONTENT,
			WindowManager.LayoutParams.WRAP_CONTENT,
			LAYOUT_FLAG,
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
			PixelFormat.TRANSLUCENT);
		
		scaleDown = ObjectAnimator.ofPropertyValuesHolder(mNext,
			PropertyValuesHolder.ofFloat("scaleX", 1.0f, .2f),
			PropertyValuesHolder.ofFloat("scaleY", 1.0f, .2f));
		scaleDown.setInterpolator(new CycleInterpolator(5));
		scaleDown.setDuration(2500);
		
		mgr = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		mWindowManagerNext = (WindowManager) getSystemService(WINDOW_SERVICE);
		
		if (mWindowManagerNext != null) {
			paramsNext.gravity = Gravity.BOTTOM | Gravity.END;
			mWindowManagerNext.addView(mNext, paramsNext);
		}
		params.alpha = 0;
		params.gravity = Gravity.TOP | Gravity.START;
		handler = new Handler(Looper.getMainLooper());
		mNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						isPlayed= false;
						try {
							handler.removeCallbacks(runnableSpeak);
						} catch (Exception ignored){
							Log.d("SPEAK RUNNABLE", "NO RUNNABLE ATTACHED");
						}
						startCapture();
					}
				});
			}
		});
		mNext.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						initialX = paramsNext.x;
						initialY = paramsNext.y;
						initialTouchX = event.getRawX();
						initialTouchY = event.getRawY();
						//lastAction = event.getAction();
						Log.d("paramsNext INITIAL ", paramsNext.x+" "+paramsNext.y);
						return true;
					case MotionEvent.ACTION_MOVE:
						paramsNext.x = initialX - (int) (event.getRawX() - initialTouchX);
						paramsNext.y = initialY - (int) (event.getRawY() - initialTouchY);
						Log.d("paramsNext ", paramsNext.x+" "+paramsNext.y);
						mWindowManagerNext.updateViewLayout(mNext, paramsNext);
						if(paramsNext.x<-200 || paramsNext.y<-200){
							mWindowManagerNext.removeView(mNext);
							HeadServiceAgri.this.stopSelf();
						}
						//lastAction = event.getAction();
						return true;
					case MotionEvent.ACTION_UP:
						int diffX = (int) (event.getRawX() - initialTouchX);
						int diffY = (int) (event.getRawY() - initialTouchY);
						if(diffX<10 && diffY<10)
							v.performClick();
						//lastAction = event.getAction();
						return true;
				}
				return false;
			}
		});
		handler.postDelayed(runnableSpeak, helpDelay);
	}
	
	@Override
	public int onStartCommand(Intent i, int flags, int startId) {
		Log.d("SERVICE", "Starting command");
		resultCode = i.getIntExtra(EXTRA_RESULT_CODE, 1337);
		resultData = i.getParcelableExtra(EXTRA_RESULT_INTENT);
		return (START_NOT_STICKY);
	}
	
	WindowManager getWindowManager() {
		return (mWindowManager);
	}
	
	Handler getHandler() {
		return (handler);
	}
	
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			tts.setLanguage(Locale.forLanguageTag("hin"));
			tts.setSpeechRate(0.9f);
			paramsTTS = new HashMap<>();
			paramsTTSSpeak = new HashMap<>();
			paramsTTS.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MyText");
			paramsTTSSpeak.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "HelpSpeak");
			tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
				@Override
				public void onStart(String utteranceId) { }
				
				@Override
				public void onDone(String utteranceId) {
					Log.d("VIEW", "REMOVE");
					try {
						if(utteranceId.equals("MyText")){
							mWindowManager.removeView(mPointer);
							handler.postDelayed(runnableSpeak, helpDelay);
						}
						else if(utteranceId.equals("HelpSpeak")) {
							handler.removeCallbacks(runnableSpeak);
							handler.postDelayed(runnableSpeak, helpDelay);
						}
					} catch (Exception e){
						Log.e("SPEAK EXCEPTION", e.getMessage());
					}
				}
				
				@Override
				public void onError(String utteranceId) {
					Log.e("ERROR",utteranceId); }
			});
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("SERVICE","DESTROY");
		try {
			if (mPointer != null) mWindowManager.removeView(mPointer);
		} catch (Exception e){ Log.e("POINTER REMOVE", e.getMessage()); }
		try {
			handler.removeCallbacksAndMessages(null);
		} catch (Exception e){ Log.e("HANDLER THREAD", e.getMessage()); }
		stopCapture();
	}
	
	private void stopCapture() {
		Log.d("CAPTURE", "Stop");
		if (projection != null) {
			projection.stop();
			vdisplay.release();
			projection = null;
		}
	}
	
	private void startCapture() {
		Log.d("CAPTURE", "Start");
		if (projection == null) {
			mWindowManagerNext.removeView(mNext);
			projection = mgr.getMediaProjection(resultCode, resultData);
			ImageTransmogrifier it = new ImageTransmogrifier(this);
			MediaProjection.Callback cb = new MediaProjection.Callback() {
				@Override
				public void onStop() {
					vdisplay.release();
				}
			};
			vdisplay = projection.createVirtualDisplay("test",
				it.getWidth(), it.getHeight(),
				getResources().getDisplayMetrics().densityDpi,
				VIRT_DISPLAY_FLAGS, it.getSurface(), null, handler);
			projection.registerCallback(cb, handler);
		}
	}
	
	void processImage(final Bitmap bitmap) {
		Log.d("PROCESS IMAGE", "I am in");
		if (!isPlayed) {
			try {
				mWindowManagerNext.addView(mNext, paramsNext);
				FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
				FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
					.getOnDeviceTextRecognizer();
				textRecognizer.processImage(image)
					.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
						@Override
						public void onSuccess(FirebaseVisionText result) {
							List<String> allKeywords = new ArrayList<String>();
							List<Point> allPositions = new ArrayList<Point>();
							for (FirebaseVisionText.TextBlock textBlock : result.getTextBlocks()) {
								for (FirebaseVisionText.Line line : textBlock.getLines()) {
									for (FirebaseVisionText.Element element : line.getElements()) {
										//Log.d("KEYWORDS", element.getText().toLowerCase());
										allKeywords.add(element.getText().toLowerCase());
										allPositions.add(Objects.requireNonNull(element.getCornerPoints())[1]);
									}
								}
							}
							for(ScreenAgri screen : MainActivity.getScreensAgri()){
								if(!screen.matchScreen(allKeywords)) continue;
								if(screen.getScreenid() == lastScreen) screen.incrementCurrentKeyword();
								else screen.resetCurrentKeyword();
								lastScreen = screen.getScreenid();
								Log.d("ON SCREEN", lastScreen+"");
								String mainKeyword = screen.getCurrentMainKeyword();
								Log.d("MAIN KEYWORD", mainKeyword);
								String textToSay = screen.getTextToSay();
								int index = allKeywords.indexOf(mainKeyword.toLowerCase());
								if(index !=- 1) {
									params.x = (int) allPositions.get(index).x;
									params.y = (int) allPositions.get(index).y;
									params.alpha = 1;
									try {
										mWindowManager.addView(mPointer, params);
									} catch (Exception ignored) {
										mWindowManager.updateViewLayout(mPointer, params);
									}
								}
								tts.speak(textToSay, TextToSpeech.QUEUE_FLUSH, paramsTTS);
								return;
							}
							Log.d("TEXT", "No text found");
							lastScreen = -1;
							tts.speak("aap galat screen pe hain", TextToSpeech.QUEUE_FLUSH, paramsTTS);
						}
					}).addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Log.d("EXCEPTION FIREBASE", e.getLocalizedMessage());
					}
				});
				
			} finally {
				stopCapture();
			}
			isPlayed = true;
		}
	}
	
}
