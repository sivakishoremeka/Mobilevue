package com.mobilevue.vod;

import java.util.HashMap;

import com.mobilevue.data.GridViewData;
import com.mobilevue.data.MovieEngine;
import com.mobilevue.data.MovieObj;
import com.mobilevue.data.ResponseObj;
import com.mobilevue.utils.CustomGridViewAdapter;
import com.mobilevue.utils.Utilities;
import com.mobilevue.vod.R;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class CategoryFragment extends Fragment {

	private static final String TAG = "CategoryFragment";
	public final static String PREFS_FILE = "PREFS_FILE";
	private final static String NETWORK_ERROR = "NETWORK_ERROR";
	private ProgressDialog mProgressDialog;
	private SearchDetails searchDtls;
	private SharedPreferences mPrefs;
	boolean D;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View rootView = inflater.inflate(R.layout.fragment_category,
				container, false);
		D = ((MyApplication) getActivity().getApplicationContext()).D;
		mPrefs = getActivity().getSharedPreferences(PREFS_FILE, 0);
		String category = mPrefs.getString("CATEGORY", "RELEASE");
		searchDtls = new SearchDetails(rootView, getArguments()
				.getInt("pageno"), category);
		getDetails(searchDtls);
		return rootView;
	}

	public void getDetails(SearchDetails sd) {
		if (D)
			Log.d(TAG, "getDetails");
		try {
			new GetDetailsAsynTask().execute(sd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class GetDetailsAsynTask extends
			AsyncTask<SearchDetails, Void, ResponseObj> {
		SearchDetails searchDetails;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (D)
				Log.d(TAG, "onPreExecute");
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			mProgressDialog = new ProgressDialog(getActivity(),
					ProgressDialog.THEME_HOLO_DARK);
			mProgressDialog.setMessage("Retrieving Details...");
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setOnCancelListener(new OnCancelListener() {

				public void onCancel(DialogInterface arg0) {
					if (mProgressDialog.isShowing())
						mProgressDialog.dismiss();
					cancel(true);
				}
			});
			mProgressDialog.show();
		}

		@Override
		protected ResponseObj doInBackground(SearchDetails... params) {
			if (D)
				Log.d(TAG, "doInBackground");
			searchDetails = (SearchDetails) params[0];
			ResponseObj resObj = new ResponseObj();

			if (Utilities.isNetworkAvailable(getActivity()
					.getApplicationContext())) {
				HashMap<String, String> map = new HashMap<String, String>();
				String androidId = Settings.Secure.getString(getActivity()
						.getApplicationContext().getContentResolver(),
						Settings.Secure.ANDROID_ID);
				map.put("TagURL", "assets?&filterType="
						+ searchDetails.category + "&pageNo="
						+ searchDetails.pageNumber + "&deviceId=" + androidId);
				resObj = Utilities.callExternalApiGetMethod(getActivity()
						.getApplicationContext(), map);
			} else {
				resObj.setFailResponse(100, NETWORK_ERROR);
			}
			return resObj;
		}

		@Override
		protected void onPostExecute(ResponseObj resObj) {
			super.onPostExecute(resObj);
			if (D)
				Log.d(TAG, "onPostExecute");
			if (resObj.getStatusCode() == 200) {
				if (D)
					Log.d(TAG, resObj.getsResponse());
				updateDetails(resObj.getsResponse(), searchDetails.rootview);
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}

			} else {
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				Toast.makeText(getActivity(), resObj.getsErrorMessage(),
						Toast.LENGTH_LONG).show();

			}
		}

		public void updateDetails(String result, View rootview) {
			if (D)
				Log.d(TAG, "updateDetails" + result);
			if (result != null) {
				final GridViewData gvDataObj = MovieEngine
						.parseMovieDetails(result);
				searchDtls.pageNumber = gvDataObj.getPageNumber();
				final GridView gridView = (GridView) (rootview
						.findViewById(R.id.f_category_gv_movies));
				gridView.setAdapter(new CustomGridViewAdapter(gvDataObj
						.getMovieListObj(), getActivity()));
				gridView.setDrawSelectorOnTop(true);
				gridView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent,
							View imageVw, int position, long arg3) {

						Intent intent = new Intent(getActivity(),
								VodMovieDetailsActivity.class);
						MovieObj movObj = gvDataObj.getMovieListObj().get(
								position);
						intent.putExtra("MediaId", movObj.getId() + "");
						intent.putExtra("EventId", movObj.getEventId() + "");
						startActivity(intent);
					}
				});
				gridView.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						if (arg1 != null) {
							arg1.startAnimation(AnimationUtils.loadAnimation(
									getActivity(), R.anim.zoom_selection));
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});
			}
		}
	}

	public class SearchDetails {
		public View rootview;
		public int pageNumber;
		public String category;

		public SearchDetails(View v, int pno, String category) {
			this.rootview = v;
			this.pageNumber = pno;
			this.category = category;
		}
	}

}
