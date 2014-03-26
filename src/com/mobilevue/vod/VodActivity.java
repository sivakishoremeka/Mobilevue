package com.mobilevue.vod;

import java.util.HashMap;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.mobilevue.adapter.MyFragmentPagerAdapter;
import com.mobilevue.adapter.VodCategoryAdapter;
import com.mobilevue.data.GridViewData;
import com.mobilevue.data.MovieEngine;
import com.mobilevue.data.ResponseObj;
import com.mobilevue.utils.Utilities;

public class VodActivity extends FragmentActivity implements
		SearchView.OnQueryTextListener {
	private static final String TAG = "VodActivity";
	public static int ITEMS;
	private final static String PREFS_FILE = "PREFS_FILE";
	private final static String NETWORK_ERROR = "NETWORK_ERROR";
	private final static String CATEGORY = "CATEGORY";
	MyFragmentPagerAdapter mAdapter;
	ViewPager mPager;
	private SharedPreferences mPrefs;
	private Editor mPrefsEditor;
	private SearchView mSearchView;
	ListView listView;
	private ProgressDialog mProgressDialog;
	boolean D;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vod);
		D = ((MyApplication) getApplicationContext()).D;
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		mPrefs = getSharedPreferences(PREFS_FILE, 0);
		mPrefsEditor = mPrefs.edit();
		mPrefsEditor.putString(CATEGORY,"RELEASE");
		mPrefsEditor.commit();
		listView = (ListView) findViewById(R.id.a_vod_lv_category);
		String[] arrMovCategNames = getResources().getStringArray(
				R.array.arrMovCategNames);
		VodCategoryAdapter categAdapter = new VodCategoryAdapter(this,
				arrMovCategNames);
		listView.setAdapter(categAdapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listView.setItemChecked(0, true);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				((AbsListView) arg0).setItemChecked(arg2, true);
				String[] arrMovCategValues = getResources().getStringArray(
						R.array.arrMovCategValues);
				mPrefsEditor.putString(CATEGORY, arrMovCategValues[arg2]);
				mPrefsEditor.commit();
				setPageCountAndGetDetails();
			}
		});
		setPageCountAndGetDetails();
		mPager = (ViewPager) findViewById(R.id.a_vod_pager);
		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				Button button = (Button) findViewById(R.id.a_vod_btn_pgno);
				button.setText("" + (arg0 + 1));
			}

			@Override
			public void onPageSelected(int arg0) {
			}
		});
		Button button = (Button) findViewById(R.id.a_vod_btn_first);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mPager.setCurrentItem(0);
			}
		});
		button = (Button) findViewById(R.id.a_vod_btn_last);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mPager.setCurrentItem(ITEMS - 1);
			}
		});
	}

	protected void setPageCountAndGetDetails() {
		if (D)
			Log.d(TAG, "setPageCountAndGetDetails");
		mPrefs = getSharedPreferences(PREFS_FILE, 0);
		String category = mPrefs.getString(CATEGORY, "");
		new GetPageCountAndGetDetailsAsynTask()
				.execute(category.equals("") ? "RELEASE" : category);
	}

	private class GetPageCountAndGetDetailsAsynTask extends
			AsyncTask<String, Void, ResponseObj> {
		String TAG = "GetPageCountAsynTask";

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (D)
				Log.d(TAG, "onPreExecute");

			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			mProgressDialog = new ProgressDialog(VodActivity.this,
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
		protected ResponseObj doInBackground(String... params) {
			if (D)
				Log.d(TAG, "doInBackground");
			String category = params[0];
			ResponseObj resObj = new ResponseObj();

			if (Utilities.isNetworkAvailable(getApplicationContext())) {
				HashMap<String, String> map = new HashMap<String, String>();
				String androidId = Settings.Secure.getString(
						getApplicationContext().getContentResolver(),
						Settings.Secure.ANDROID_ID);
				map.put("TagURL", "assets?&filterType=" + category + "&pageNo="
						+ 0 + "&deviceId=" + androidId);
				resObj = Utilities.callExternalApiGetMethod(
						getApplicationContext(), map);
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
				updatePageCountAndDetails(resObj.getsResponse());
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}

			} else {
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				Toast.makeText(VodActivity.this, resObj.getsErrorMessage(),
						Toast.LENGTH_LONG).show();
			}
		}

		public void updatePageCountAndDetails(String result) {
			if (D)
				Log.d(TAG, "updateDetails" + result);
			if (result != null) {
				GridViewData gvDataObj = MovieEngine.parseMovieDetails(result);
				ITEMS = gvDataObj.getPageCount();
				mAdapter = new MyFragmentPagerAdapter(
						getSupportFragmentManager());
				mPager.setAdapter(mAdapter);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nav_menu, menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		mSearchView = (SearchView) searchItem.getActionView();
		setupSearchView(searchItem);
		MenuItem refreshItem = menu.findItem(R.id.menu_btn_refresh);
		refreshItem.setVisible(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			break;
		case R.id.menu_btn_home:
			NavUtils.navigateUpFromSameTask(this);
			break;
		default:
			break;
		}
		return true;
	}

	private void setupSearchView(MenuItem searchItem) {
		mSearchView.setOnQueryTextListener(this);
	}

	protected boolean isAlwaysExpanded() {
		return false;
	}

	@Override
	public boolean onQueryTextChange(String arg0) {
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String movieName) {
		mSearchView.clearFocus();
		listView.clearChoices();
		mPrefs = getSharedPreferences(PREFS_FILE, 0);
		mPrefsEditor = mPrefs.edit();
		mPrefsEditor.putString(CATEGORY, movieName);
		mPrefsEditor.commit();
		setPageCountAndGetDetails();
		return false;
	}
}