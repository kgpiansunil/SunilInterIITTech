package iitkgp.com.test;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.CycleInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import iitkgp.com.test.adapters.MyListAdaptar;

public class MainActivity extends AppCompatActivity implements OnInitListener{
	
	private int[] sids = {R.drawable.agriculture, R.drawable.cabs, R.drawable.edication, R.drawable.recharge, R.drawable.reservations, R.drawable.shopping};
	private int[] hids = {R.drawable.agricultureh, R.drawable.laptop, R.drawable.medical, R.drawable.microwave, R.drawable.printer, R.drawable.remote};
	private String[] bgcolorS = {"#B3DECC", "#DDC7E1", "#F2BBB3", "#C9E199", "#C9DBF1", "#F2D6A4"};
	private String[] bgcolorH = {"#F3D6A4", "#C9E199", "#F2BAB3", "#DEC8E2", "#B3DECC", "#C9DCF2"};
	
	private List<String> speechS = Arrays.asList("कृषि के लिए यहां क्लिक करें", "कैब के लिए यहां क्लिक करें", "शिक्षा के लिए यहां क्लिक करें", "रिचार्ज के लिए यहां क्लिक करें", "आरक्षण के लिए यहां क्लिक करें", "खरीदारी के लिए यहां क्लिक करें");
	private List<String> speechH = Arrays.asList("कृषि के लिए यहां क्लिक करें", "लैपटॉप के लिए यहां क्लिक करें", "चिकित्सा उपकरणों के लिए यहां क्लिक करें", "माइक्रोवेव के लिए यहां क्लिक करें", "प्रिंटर के लिए यहां क्लिक करें", "रिमोट के लिए यहां क्लिक करें");
	
	private ImageView software, hardware, logo;
	private RecyclerView recyclerView;
	
	private Handler handler;
	
	private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
	private static final int REQUEST_SCREENSHOT = 59706;
	private static final int NO_OF_LIST_ITEM = 6;
	private static final int READ_WRITE_EXTERNAL_STORAGE = 45210;
	int[] permissions = {0, 0};
	private int state = 0;
	
	private TextToSpeech textToSpeech;
	private HashMap<String, String> paramsTTS = new HashMap<>();
	
	private static List<Screen> screens = null;
	private static List<ScreenAgri> screenAgris = null;
	private Point size;
	private int c = 0;
	
	private MyListAdaptar myListAdaptar;
	
	final Runnable runnableSpeak = new Runnable() {
		public void run() {
			Log.d("Runnable","Runnable is working");
			paramsTTS.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Apps");
			textToSpeech.speak("ऐप्स पर गाइड के लिए यहां क्लिक करें", TextToSpeech.QUEUE_FLUSH, paramsTTS);
			handler.postDelayed(runnableSpeak, LOOPING_DELAY);
		}
	};
	
	private static final int INITIAL_DELAY = 1000;
	private static final int LOOPING_DELAY = 10000;
	private static final int ANIMATION_PERIOD = 3000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
		Objects.requireNonNull(getSupportActionBar()).hide();
		
		setContentView(R.layout.activity_main);
		
		MediaProjectionManager mgr = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
		handler = new Handler(Looper.getMainLooper());
		
		size = new Point();
		getWindowManager().getDefaultDisplay().getSize(size);
		
		recyclerView = findViewById(R.id.recylerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		
		
		software = findViewById(R.id.software);
		hardware = findViewById(R.id.hardware);
		logo = findViewById(R.id.logo);
		
		if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
			ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
			ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.REQUEST_INSTALL_PACKAGES) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.REQUEST_INSTALL_PACKAGES},
				READ_WRITE_EXTERNAL_STORAGE);
		} else installApk();
		
		if (!Settings.canDrawOverlays(this)) {
			Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
				Uri.parse("package:" + getPackageName()));
			startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
		} else permissions[0] = 1;
		
		startActivityForResult(Objects.requireNonNull(mgr).createScreenCaptureIntent(),
			REQUEST_SCREENSHOT);
		
		textToSpeech = new TextToSpeech(MainActivity.this, this);
		handler.postDelayed(runnableSpeak, INITIAL_DELAY);
	}
	
	@Override
	public void onPause() {
		textToSpeech.stop();
		handler.removeCallbacks(runnableSpeak);
		super.onPause();
	}
	
	@Override
	public void onResume() {
		handler.postDelayed(runnableSpeak, INITIAL_DELAY);
		super.onResume();
	}
	
	public void scaleView(final View view) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(view,
					PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.9f),
					PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.9f));
				scaleDown.setDuration(ANIMATION_PERIOD);
				scaleDown.setAutoCancel(true);
				scaleDown.setInterpolator(new CycleInterpolator(3));
				scaleDown.start();
			}
		});
	}
	
	private void setVisibility() {
		handler.removeCallbacks(runnableSpeak);
		c=0;
		try {
			textToSpeech.stop();
		} catch (Exception ignored) {}
		switch (state){
			case 0: recyclerView.setVisibility(View.VISIBLE);
					software.setVisibility(View.GONE);
					hardware.setVisibility(View.GONE);
					logo.setVisibility(View.GONE);
					state = 1;
					break;
			case 1: recyclerView.setVisibility(View.GONE);
					software.setVisibility(View.VISIBLE);
					hardware.setVisibility(View.VISIBLE);
					logo.setVisibility(View.VISIBLE);
					handler.postDelayed(runnableSpeak, INITIAL_DELAY);
					state = 0;
					break;
		}
	}
	
	private void installApk(){
		/*Log.d("ASSETS", "Install required");
		try {
			Log.d("ASSETS", "Starting copy");
			InputStream myInput = getAssets().open("hardware.apk");
			String outFileName = Environment.getExternalStorageDirectory() + java.io.File.separator + "hardware.apk";
			OutputStream myOutput = new FileOutputStream(outFileName);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = myInput.read(buffer)) > 0) {
				myOutput.write(buffer, 0, length);
			}
			myOutput.flush();
			myOutput.close();
			myInput.close();
			
			Log.d("ASSETS", "copied file");
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", new File(outFileName));
			intent.setDataAndType(uri, "application/vnd.android.package-archive");
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			startActivity(intent);
			
		} catch (IOException e){
			e.printStackTrace();
		}*/
	}
	
	public static List<Screen> getScreens() {
		if (screens == null) screens = createScreenObjects();
		return screens;
	}
	
	public static List<ScreenAgri> getScreensAgri() {
		if (screenAgris == null) screenAgris = createScreenObjectsAgri();
		return screenAgris;
	}
	
	private static List<Screen> createScreenObjects() {
		Screen homebase = new Screen(null, Collections.singletonList(new MainKeyword("Home", "", Collections.singletonList(""))), Arrays.asList("Home", "Shop", "Bank", "Inbox"), 4, 0, Collections.singletonList("पहली स्क्रीन पर जाने के लिए कृपया यहां क्लिक करें"), 200);
		Screen rechargebase = new Screen(null, Collections.singletonList(new MainKeyword("Mobile", "", Arrays.asList("DTH", "Electricity", "Piped", "Water", "Apartments", "Broadband", "Loan", "Insurance", "Data", "Cable", "Municipal", "Toll", "Challan"))), Arrays.asList("Mobile", "DTH", "Electricity", "Piped", "Water", "Apartments", "Broadband", "Loan", "Insurance", "Data", "Cable", "Municipal", "Toll", "Challan"), 3, 1, Collections.singletonList("मोबाइल रिचार्ज करने के लिए सही स्वाइप करें"), 201);
		Screen home = new Screen(homebase, Collections.singletonList(new MainKeyword("Mobile", "Movie", Collections.singletonList(""))), Arrays.asList("Pay", "Transfer", "Passbook", "Money", "Link", "Favourite", "Lifafa", "Accept", "UPI", "KYC", "First"), 3, 0, Collections.singletonList("अपने मोबाइल रिचार्ज के लिए यहां क्लिक करें"), 100);
		Screen mobileRecharge = new Screen(rechargebase, Arrays.asList(new MainKeyword("Prepaid", "Postpaid", Collections.singletonList("")), new MainKeyword("Enter", "", Collections.singletonList("Proceed"))), Arrays.asList("Prepaid", "Postpaid", "Bill", "Opostpaid", "Oprepaid"), 3, 0, Arrays.asList("प्रीपेड रिचार्ज के लिए यहां क्लिक करें", "कृपया ऊपर दिए गए बॉक्स में अपना मोबाइल नंबर दर्ज करें"), 101);
		Screen screen2 = new Screen(null, Collections.singletonList(new MainKeyword("Enter", "", Collections.singletonList("+91"))), Arrays.asList("Contacts", "Enter"), 2, 0, Collections.singletonList("या तो मोबाइल नंबर लिखें या संपर्कों से चुनें"), 2);
		Screen screen3 = new Screen(null, Collections.singletonList(new MainKeyword("Browse", "", Collections.singletonList(""))), Arrays.asList("Operator", "Browse", "Forward"), 3, 0, Collections.singletonList("या तो राशि लिखें या यहां से योजना चुनें"), 3);
		Screen screen4 = new Screen(null, Collections.singletonList(new MainKeyword("Plan", "", Collections.singletonList(""))), Arrays.asList("Plans", "Browse", "Plan"), 3, 0, Collections.singletonList("ब्राउज़र प्लान चुनें या राशि लिखें"), 4);
		return Arrays.asList(home, mobileRecharge, screen2, screen3, screen4);
	}
	
	private static List<ScreenAgri> createScreenObjectsAgri() {
		ScreenAgri homeScreen = new ScreenAgri(Collections.singletonList("now"), Arrays.asList("now", "package", "calendar", "settings", "news"), Collections.singletonList("Fertilizer kharid ne ke liye yaha dabayeen"), 0);
		ScreenAgri buyScreen = new ScreenAgri(Collections.singletonList("seller"), Arrays.asList("seller", "top", "liked", "most", "deals"), Collections.singletonList("Tabs pe click kaare ya scroll kar ke fertilizers chuniye"), 1);
		ScreenAgri productScreen = new ScreenAgri(Collections.singletonList("cart"), Arrays.asList("description", "cart", "likes", "product"), Collections.singletonList("Button pe dabaye and cart pe jaaye"), 2);
		ScreenAgri cartScreen = new ScreenAgri(Arrays.asList("checkout", "explore"), Arrays.asList("cart", "explore", "checkout"), Arrays.asList("Kharid ne ke liye yaha dabayeen", "explore karne ke liye yaha dabayeen"), 3);
		ScreenAgri addressScreen = new ScreenAgri(Collections.singletonList("next"), Arrays.asList("next", "shipping"), Collections.singletonList("address bhare aur yaha dabayeen"), 4);
		ScreenAgri addressScreen2 = new ScreenAgri(Collections.singletonList("next"), Arrays.asList("next", "billing"), Collections.singletonList("address bhare aur yaha dabayeen"), 5);
		ScreenAgri addressScreen3 = new ScreenAgri(Collections.singletonList("next"), Arrays.asList("next", "shipping", "actual"), Collections.singletonList("yaha dabayeen"), 6);
		ScreenAgri orderScreen = new ScreenAgri(Collections.singletonList("order"), Arrays.asList("checkout", "cancel", "order", "now"), Collections.singletonList("scroll kar ke coupon code aur payment method daalein aur yaha dabayee"), 7);
		return Arrays.asList(homeScreen, buyScreen, productScreen, cartScreen, addressScreen, addressScreen2, addressScreen3, orderScreen);
	}
	
	private void initializeView(final int resultCode, final Intent data) {
		if (permissions[0] != 1 || permissions[1] != 1) return;
		
		software.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setVisibility();
				myListAdaptar = new MyListAdaptar(MainActivity.this, sids, size, resultCode, data, textToSpeech, speechS, Arrays.asList(bgcolorS));
				recyclerView.setAdapter(myListAdaptar);
				setLayoutListner();
			}
		});
		
		hardware.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setVisibility();
				myListAdaptar = new MyListAdaptar(MainActivity.this, hids, size, resultCode, data, textToSpeech, speechH, Arrays.asList(bgcolorH));
				recyclerView.setAdapter(myListAdaptar);
				setLayoutListner();
			}
		});
		
		
	}
	
	private void setLayoutListner(){
		try {
			textToSpeech.stop();
		} catch (Exception ignored) {}
		recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				scaleView(Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(c)).itemView);
				handler.postDelayed(myListAdaptar.getRunnable(c), INITIAL_DELAY);
				recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION || requestCode == REQUEST_SCREENSHOT) {
			if (resultCode == RESULT_OK) {
				if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) permissions[0] = 1;
				else permissions[1] = 1;
				if (permissions[0] == 1 && permissions[1] == 1) {
					initializeView(resultCode, data);
				}
			} else {
				Toast.makeText(this,
					"Draw over other app permission not available. Closing the application",
					Toast.LENGTH_SHORT).show();
				finish();
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	@Override
	protected void onDestroy() {
		handler.removeCallbacksAndMessages(null);
		if(textToSpeech != null) {
			textToSpeech.stop();
			textToSpeech.shutdown();
			Log.d("TTS", "TTS Destroyed");
		}
		super.onDestroy();
	}
	
	private void removeAnimation(){
		software.setAnimation(null);
		hardware.setAnimation(null);
	}
	
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			textToSpeech.setLanguage(Locale.forLanguageTag("hin"));
			textToSpeech.setSpeechRate(0.9f);
			textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
				@Override
				public void onStart(String utteranceId) {
					if(utteranceId.equals("Apps"))
						scaleView(software);
					else if(utteranceId.equals("Devices"))
						scaleView(hardware);
				}
				
				@Override
				public void onDone(String utteranceId) {
					removeAnimation();
					if(utteranceId.equals("Apps")){
						paramsTTS.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Devices");
						textToSpeech.speak("उपकरणों पर गाइड के लिए यहां क्लिक करें", TextToSpeech.QUEUE_FLUSH, paramsTTS);
					}else if(utteranceId.equals("ListText")){
						c++;
						Log.d("C - Value", c + "");
						if(c==NO_OF_LIST_ITEM){
							c=0;
							Log.d("C - Value", "RESSETING");
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									scaleView(Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(c)).itemView);
								}
							}, LOOPING_DELAY);
							handler.postDelayed(myListAdaptar.getRunnable(c), LOOPING_DELAY);
							return;
						}
						if(recyclerView.findViewHolderForAdapterPosition(c) != null){
							scaleView(Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(c)).itemView);
							handler.post(myListAdaptar.getRunnable(c));
						}
					}
				}
				
				@Override
				public void onError(String utteranceId) {
					Log.e("ERROR",utteranceId);
				}
			});
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode,
										   @NonNull String[] permissions, @NonNull int[] grantResults) {
		Log.d("PERMISSION", requestCode+"");
		Log.d("PERMISSION EXPECTED", READ_WRITE_EXTERNAL_STORAGE +"");
		if(requestCode == READ_WRITE_EXTERNAL_STORAGE && grantResults.length > 0
			&& grantResults[0] == PackageManager.PERMISSION_GRANTED)
			installApk();
	}
	
	@Override
	public void onBackPressed() {
		Log.d("ON BACK PRESSED", state+"");
		if(state == 1){
			setVisibility();
		} else super.onBackPressed();
	}
}