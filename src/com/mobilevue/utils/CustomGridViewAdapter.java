package com.mobilevue.utils;

import java.util.List;

import com.mobilevue.data.MovieObj;
import com.mobilevue.imagehandler.SmartImageView;
import com.mobilevue.vod.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;

public class CustomGridViewAdapter extends BaseAdapter {
	private List<MovieObj> movieList;
	private LayoutInflater inflater;

	public CustomGridViewAdapter(List<MovieObj> listMovies, Activity context) {
		this.movieList = listMovies;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {

		return movieList.size();
	}

	@Override
	public Object getItem(int position) {

		return movieList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.vod_gridview_item, null);
		MovieObj movBean = movieList.get(position);
		SmartImageView siv = ((SmartImageView) layout
				.findViewById(R.id.vod_gv_item_img));
		siv.setImageUrl(movBean.getImage());
		RatingBar ratingBar = (RatingBar) layout
				.findViewById(R.id.vod_gv_item_rating_bar);
		ratingBar.setRating(Float.parseFloat(movBean.getRating()));
		return layout;
	}
}
