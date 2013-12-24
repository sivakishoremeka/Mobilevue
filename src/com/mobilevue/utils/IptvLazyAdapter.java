package com.mobilevue.utils;

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

import com.mobilevue.vod.EpgDetailsActivity;
import com.mobilevue.vod.R;
import com.mobilevue.vod.VideoPlayerActivity;

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
		
		TextView title = (TextView) vi.findViewById(R.id.iptv_list_row_tv_ch_name); 
		ImageView thumb_image = (ImageView) vi.findViewById(R.id.iptv_list_row_iv_ch_image);
        Button watch = (Button)vi.findViewById(R.id.iptv_list_row_btn_watch);  
        watch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(activity,
						VideoPlayerActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt("CLIENTID", clientId);
				bundle.putString("VIDEOTYPE","LIVETV");
				bundle.putString("URL", vod.get(KEY_VIDEO_URL));
				intent.putExtras(bundle);
				activity.startActivity(intent);

				
			}
		});
        
        Button btn_EPG_Details = (Button)vi.findViewById(R.id.iptv_list_row_btn_epg); 
        btn_EPG_Details.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity,
						EpgDetailsActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString(KEY_TITLE, vod.get(KEY_TITLE));
				intent.putExtras(bundle);
				activity.startActivity(intent);
			}
		});
		title.setText(vod.get(KEY_TITLE));
		imageLoader.DisplayImage(vod.get(KEY_THUMB_URL), thumb_image);
		return vi;
	}
}
