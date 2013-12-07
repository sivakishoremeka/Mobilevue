package com.mobilevue.vod;

import java.util.List;

import com.mobilevue.data.MovieObj;
import com.mobilevue.imagehandler.SmartImageView;
import com.mobilevue.vod.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
		RelativeLayout layout = (RelativeLayout) inflater.inflate(
				R.layout.vod_gridview_item, null);
		MovieObj movBean = movieList.get(position);
		SmartImageView siv = ((SmartImageView) layout
				.findViewById(R.id.gridview_img));
		siv.setImageUrl(movBean.getImage());
		((RatingBar) layout.findViewById(R.id.gridview_rating_bar))
				.setRating(Float.parseFloat(movBean.getRating()));
		return layout;
	}
}