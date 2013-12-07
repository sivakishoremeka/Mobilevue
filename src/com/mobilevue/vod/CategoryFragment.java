package com.mobilevue.vod;

import java.util.HashMap;

import com.mobilevue.data.GridViewData;
import com.mobilevue.data.MovieEngine;
import com.mobilevue.data.MovieObj;
import com.mobilevue.data.ResponseObj;
import com.mobilevue.utils.MySSLSocketFactory;
import com.mobilevue.utils.Utilities;
import com.mobilevue.vod.R;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
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
import android.widget.Button;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class CategoryFragment extends Fragment {

	private static final String TAG = "CategoryFragment";
	private final static String NETWORK_ERROR = "NETWORK_ERROR";
	private ProgressDialog mProgressDialog;
	int totalPageCount;
	SearchDetails searchDtls;
	String category = "";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View rootView = inflater.inflate(R.layout.fragment_category,
				container, false);
		searchDtls = new SearchDetails(rootView, 0, getArguments().getString(
				"category"));
		getDetails(searchDtls);
		((Button) rootView.findViewById(R.id.lf_button))
				.setVisibility(View.INVISIBLE);
		((Button) rootView.findViewById(R.id.rt_button))
				.setVisibility(View.INVISIBLE);
		((Button) rootView.findViewById(R.id.lf_button))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (searchDtls.pageNumber > 0) {
							searchDtls.pageNumber = searchDtls.pageNumber - 1;
							getDetails(searchDtls);
						}

					}
				});
		((Button) rootView.findViewById(R.id.rt_button))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (searchDtls.pageNumber < totalPageCount - 1) {
							searchDtls.pageNumber = searchDtls.pageNumber + 1;
							getDetails(searchDtls);
						}
					}
				});
		return rootView;
	}

	public void getDetails(SearchDetails sd) {
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
			Log.d(TAG, "onPostExecute");
			if (resObj.getStatusCode() == 200) {
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
			Log.d(TAG, "updateDetails" + result);
			if (result != null) {
				final GridViewData gvDataObj = MovieEngine
						.parseMovieDetails(result);
				totalPageCount = gvDataObj.getPageCount();
				searchDtls.pageNumber = gvDataObj.getPageNumber();
				// TextView page_no =
				// (TextView)(rootview.findViewById(R.id.home_page_no));
				// page_no.setText((pageNumber+1)+"/"+totalPageCount);
				final GridView gridView = (GridView) (rootview
						.findViewById(R.id.gridview_new_release));
				gridView.setAdapter(new CustomGridViewAdapter(gvDataObj
						.getMovieListObj(), getActivity()));
				gridView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent,
							View imageVw, int position, long arg3) {
						MovieObj movieObj = (MovieObj) (parent
								.getItemAtPosition(position));
						String eventid = movieObj.getEventId() + "";
						Intent intent = new Intent(getActivity(),
								OrderEventActivity.class);// VideoPlayerUrlActivity.class);
						Bundle bundle = new Bundle();
						bundle.putString("eventid", eventid);
						bundle.putInt(
								"CLIENTID",
								getActivity().getSharedPreferences(
										AuthenticationAcitivity.PREFS_FILE, 0)
										.getInt("CLIENTID", 0)); // client id
																	// hardcoded
						intent.putExtras(bundle);
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
				// findViewById(R.id.srch_layout).setVisibility(View.VISIBLE);
				// findViewById(R.id.frag_rel_layout).setVisibility(View.VISIBLE);

				Button left_btn = (Button) rootview
						.findViewById(R.id.lf_button);
				Button right_btn = (Button) rootview
						.findViewById(R.id.rt_button);
				if (searchDtls.pageNumber == 0
						&& searchDtls.pageNumber == totalPageCount - 1) {
					left_btn.setVisibility(View.INVISIBLE);
					right_btn.setVisibility(View.INVISIBLE);
				} else if (searchDtls.pageNumber == 0) {
					left_btn.setVisibility(View.INVISIBLE);
					right_btn.setVisibility(View.VISIBLE);
				} else if (searchDtls.pageNumber == totalPageCount - 1) {
					left_btn.setVisibility(View.VISIBLE);
					right_btn.setVisibility(View.INVISIBLE);
				} else if ((left_btn.getVisibility() == View.INVISIBLE)
						&& (right_btn.getVisibility() == View.INVISIBLE)) {
					left_btn.setVisibility(View.VISIBLE);
					right_btn.setVisibility(View.VISIBLE);
				} else if (left_btn.getVisibility() == View.INVISIBLE) {
					left_btn.setVisibility(View.VISIBLE);
				} else if (right_btn.getVisibility() == View.INVISIBLE) {
					right_btn.setVisibility(View.VISIBLE);
				}

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
