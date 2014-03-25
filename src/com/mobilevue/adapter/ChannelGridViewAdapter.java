package com.mobilevue.adapter;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mobilevue.data.ChannelData;
import com.mobilevue.vod.R;

public class ChannelGridViewAdapter extends BaseAdapter {
	private List<ChannelData> channelList;
	private LayoutInflater inflater;

	public ChannelGridViewAdapter(List<ChannelData> channelList,
			Activity context) {
		this.channelList = channelList;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {

		return channelList.size();
	}

	@Override
	public Object getItem(int position) {

		return channelList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.ch_gridview_item, null);
		ChannelData chObj = channelList.get(position);
		ImageView siv = ((ImageView) layout.findViewById(R.id.ch_gv_item_img));
		siv.setPadding(2, 2, 2, 2);
		com.nostra13.universalimageloader.core.ImageLoader.getInstance()
				.displayImage(chObj.getImage(), siv);
		/*
		 * TextView chName = (TextView) layout.findViewById(R.id.ch_gv_tv_name);
		 * chName.setText(chObj.getChannelName());
		 */
		return layout;
	}
}
