package iitkgp.com.test.adapters;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import iitkgp.com.test.HeadService;
import iitkgp.com.test.HeadServiceAgri;
import iitkgp.com.test.MainActivity;
import iitkgp.com.test.R;

public class MyListAdaptar extends RecyclerView.Adapter<MyListAdaptar.ListViewHolder> {
	
	private List<Integer> arrayList;
	private List<String> colorCodes;
	private MainActivity context;
	private Point size;
	private int resultCode;
	private Intent data;
	private ArrayList<Runnable> runnables = new ArrayList<>();
	private TextToSpeech textToSpeech;
	
	private List<String> textToSays;
	
	public MyListAdaptar(MainActivity context, int[] ids, Point size, int resultCode, Intent data, TextToSpeech textToSpeech, List<String> textToSays, List<String> colorCodes) {
		this.context = context;
		this.arrayList = Ints.asList(ids);
		this.size = size;
		this.resultCode = resultCode;
		this.data = data;
		this.textToSpeech = textToSpeech;
		this.textToSays = textToSays;
		this.colorCodes = colorCodes;
	}
	
	public Runnable getRunnable(int position){
		return runnables.get(position);
	}
	
	@NonNull
	@Override
	public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View listEditableView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_view, parent, false);
		RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) ((size.y) / arrayList.size() - 10));
		listEditableView.setLayoutParams(lp);
		return new ListViewHolder(listEditableView);
	}
	
	@Override
	public void onBindViewHolder(@NonNull ListViewHolder holder, final int position) {
		holder.imageView.setImageResource(arrayList.get(position));
		Log.d("COLOR CODE", colorCodes.get(position));
		holder.cardView.setCardBackgroundColor(Color.parseColor(colorCodes.get(position).toLowerCase()));
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(context, "Selected position "+position, Toast.LENGTH_SHORT).show();
				if(arrayList.get(position).equals(R.drawable.recharge)){
					String appPackageName = "net.one97.paytm";
					PackageManager pm = context.getApplicationContext().getPackageManager();
					Intent appstart = pm.getLaunchIntentForPackage(appPackageName);
					if (null != appstart) {
						context.getApplicationContext().startActivity(appstart);
					} else {
						Toast.makeText(context.getApplicationContext(), "Install PayTm on your device", Toast.LENGTH_SHORT).show();
					}
					Intent i = new Intent(context, HeadService.class)
						.putExtra(HeadService.EXTRA_RESULT_CODE, resultCode)
						.putExtra(HeadService.EXTRA_RESULT_INTENT, data);
					Log.d("SERVICE", "Starting Service");
					context.startService(i);
					context.finish();
					
				} else if(arrayList.get(position).equals(R.drawable.agriculture)){
					String appPackageName = "com.criyagen";
					PackageManager pm = context.getApplicationContext().getPackageManager();
					Intent appstart = pm.getLaunchIntentForPackage(appPackageName);
					if (null != appstart) {
						context.getApplicationContext().startActivity(appstart);
					} else {
						Toast.makeText(context.getApplicationContext(), "Install AgriApp on your device", Toast.LENGTH_SHORT).show();
					}
					Intent i = new Intent(context, HeadServiceAgri.class)
						.putExtra(HeadService.EXTRA_RESULT_CODE, resultCode)
						.putExtra(HeadService.EXTRA_RESULT_INTENT, data);
					Log.d("SERVICE", "Starting Service Agri");
					context.startService(i);
					context.finish();
				} else if(arrayList.get(position).equals(R.drawable.printer) || arrayList.get(position).equals(R.drawable.laptop)){
					String appPackageName = "com.IITKgp.InterIIT_Tech";
					PackageManager pm = context.getApplicationContext().getPackageManager();
					Intent appstart = pm.getLaunchIntentForPackage(appPackageName);
					if (null != appstart)
						context.getApplicationContext().startActivity(appstart);
				}
			}
		});
		Log.d("LIST ANIMATION", "Added");
		runnables.add(getRunnable(textToSays.get(position)));
	}
	
	private Runnable getRunnable(final String textToSay){
		return new Runnable() {
			@Override
			public void run() {
				Log.d("Runnable Adapter","RUNNABLE ADAPTER is working");
				HashMap<String, String> paramsTTSSpeak = new HashMap<>();
				paramsTTSSpeak.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ListText");
				textToSpeech.speak(textToSay, TextToSpeech.QUEUE_FLUSH, paramsTTSSpeak);
			}
		};
	}
	
	@Override
	public void onViewDetachedFromWindow(final ListViewHolder holder)
	{
		holder.itemView.clearAnimation();
	}
	
	@Override
	public int getItemCount() {
		return arrayList.size();
	}
	
	static class ListViewHolder extends RecyclerView.ViewHolder {
		ImageView imageView;
		CardView cardView;
		ListViewHolder(View itemView) {
			super(itemView);
			this.imageView = (ImageView) itemView.findViewById(R.id.row_item);
			this.cardView = (CardView) itemView.findViewById(R.id.row_item_cardView);
		}
	}
}
