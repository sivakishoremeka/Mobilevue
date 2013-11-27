package com.mobilvue.utils;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobilevue.vod.R;
import com.mobilvue.vod.VideoPlayerActivity;

public class IptvLazyAdapter extends BaseAdapter {
	public static final String KEY_ID = "id";
	public static final String EVENT_ID = "event_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_ARTIST = "artist";
	public static final String KEY_DURATION = "duration";
	public static final String KEY_THUMB_URL = "thumb_url";
	public static final String KEY_VIDEO_URL = "video_url";
	private Activity activity;
	private ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;
	public int clientId;

	public IptvLazyAdapter(Activity a, ArrayList<HashMap<String, String>> d,int clientId) {
		activity = a;
		data = d;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = new ImageLoader(activity.getApplicationContext());
		this.clientId = clientId;
	}

	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.iptv_list_row, null);
		
		final HashMap<String, String> vod  = data.get(position);;
		
		TextView title = (TextView) vi.findViewById(R.id.mediatitle); // title
		TextView artist = (TextView) vi.findViewById(R.id.artist); // artist
																	// name
		TextView duration = (TextView) vi.findViewById(R.id.duration); // duration
		ImageView thumb_image = (ImageView) vi.findViewById(R.id.list_image); // thumb
																				// image
        Button watch = (Button)vi.findViewById(R.id.watch);  
        watch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(activity,
						VideoPlayerActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt("CLIENTID", clientId);
				bundle.putString("URL", vod.get(KEY_VIDEO_URL));
				intent.putExtras(bundle);
				activity.startActivity(intent);

				
			}
		});
		// Setting all values in listview
		title.setText(vod.get(KEY_TITLE));
		artist.setText(vod.get(KEY_ARTIST));
		duration.setText(vod.get(KEY_DURATION));
		imageLoader.DisplayImage(vod.get(KEY_THUMB_URL), thumb_image);
		return vi;
	}
}
