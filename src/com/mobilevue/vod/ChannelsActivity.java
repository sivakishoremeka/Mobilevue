package com.mobilevue.vod;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.mobilevue.adapter.ChannelGridViewAdapter;
import com.mobilevue.data.DeviceDatum;
import com.mobilevue.data.ServiceDatum;
import com.mobilevue.database.ServiceProvider;
import com.mobilevue.retrofit.OBSClient;

public class ChannelsActivity extends Activity implements
		LoaderCallbacks<Cursor> {

	private final String TAG = ChannelsActivity.this.getClass().getName();
	public final static String CHANNEL_EPG = "Channel Epg";
	public final static String IPTV_CHANNELS_DETAILS = "IPTV Channels Details";
	public final static String CHANNELS_UPDATED_AT = "Updated At";
	public final static String CHANNELS_LIST = "Channels";
	private ProgressDialog mProgressDialog;
	GridView gridView;

	MyApplication mApplication = null;
	OBSClient mOBSClient;
	ExecutorService mExecutorService;
	boolean mIsReqCanceled = false;

	boolean mIsLiveDataReq = false;
	int mReqType = ServiceProvider.ALLSERVICES;
	boolean mIsBalCheckReq;
	float mBalance;

	String mSearchString;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channels);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		mApplication = ((MyApplication) getApplicationContext());
		mExecutorService = Executors.newCachedThreadPool();
		mOBSClient = mApplication.getOBSClient(this, mExecutorService);
		mIsBalCheckReq = mApplication.isBalCheckReq;
		gridView = (GridView) (findViewById(R.id.a_gv_channels));

		// initiallizing req criteria
		mReqType = ServiceProvider.ALLSERVICES;
		mSearchString = null;
		CheckBalancenGetData();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d("onNewIntent", "onNewIntent");
		if (null!=intent && null!=intent.getAction() && intent.getAction().equals(Intent.ACTION_SEARCH)) {

			// initiallizing req criteria
			mReqType = ServiceProvider.SEARCH;
			mSearchString = intent.getStringExtra(SearchManager.QUERY);
			CheckBalancenGetData();
		}
	}

	private void CheckBalancenGetData() {
		if (mIsBalCheckReq)
			validateDevice();
		else
			getServices();
	}

	private void getServices() {
		getLoaderManager().restartLoader(mReqType, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.nav_menu, menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		searchItem.setVisible(true);
		MenuItem refreshItem = menu.findItem(R.id.action_refresh);
		refreshItem.setVisible(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			break;
		case R.id.action_home:
			NavUtils.navigateUpFromSameTask(this);
			break;
		case R.id.action_account:
			startActivity(new Intent(this, MyAccountActivity.class));
			break;
		case R.id.action_refresh:
			// initiallizing req criteria
			mReqType = ServiceProvider.ALLSERVICES_ON_REFRESH;
			mSearchString = null;
			CheckBalancenGetData();
			break;
		case R.id.action_search:
			 //The searchbar is initiated programmatically with a call to your Activity’s onSearchRequested method. 
			onSearchRequested();
			break;
		default:
			break;
		}
		return true;
	}

	private void updateChannels(final List<ServiceDatum> list) {
		if (list != null) {
			final GridView gridView = (GridView) (findViewById(R.id.a_gv_channels));
			gridView.setAdapter(new ChannelGridViewAdapter(list,
					ChannelsActivity.this));
			gridView.setDrawSelectorOnTop(true);
			gridView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View imageVw,
						int position, long arg3) {
					ServiceDatum service = list.get(position);
					startActivity(new Intent(ChannelsActivity.this,
							IPTVActivity.class)
							.putExtra(IPTVActivity.CHANNEL_NAME,
									service.getChannelName()).putExtra(
									IPTVActivity.CHANNEL_URL, service.getUrl()));
				}
			});
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		mProgressDialog = new ProgressDialog(ChannelsActivity.this,
				ProgressDialog.THEME_HOLO_DARK);
		mProgressDialog.setMessage("Connectiong to Server...");
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.setOnCancelListener(new OnCancelListener() {

			public void onCancel(DialogInterface arg0) {
				if (mProgressDialog.isShowing())
					mProgressDialog.dismiss();
			}
		});
		mProgressDialog.show();

		CursorLoader loader = null;
		if (id == ServiceProvider.ALLSERVICES) {
			loader = new CursorLoader(this, ServiceProvider.ALLSERVICES_URI,
					null, null, null, null);
		}
		if (id == ServiceProvider.ALLSERVICES_ON_REFRESH) {
			loader = new CursorLoader(this,
					ServiceProvider.ALLSERVICES_ONREFREFRESH_URI, null, null,
					null, null);
		}
		if (id == ServiceProvider.SEARCH) {
			if (null == mSearchString) {
				mSearchString = "";
			}
			loader = new CursorLoader(this, ServiceProvider.SEARCH_URI, null,
					null, new String[] { mSearchString }, null);
		}
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}

		int svcIdIdx = cursor.getColumnIndexOrThrow(ServiceProvider.SERVICE_ID);
		int cIdIdx = cursor.getColumnIndexOrThrow(ServiceProvider.CLIENT_ID);
		int chNameIdx = cursor
				.getColumnIndexOrThrow(ServiceProvider.CHANNEL_NAME);
		int imgIdx = cursor.getColumnIndexOrThrow(ServiceProvider.IMAGE);
		int urlIdx = cursor.getColumnIndexOrThrow(ServiceProvider.URL);
		List<ServiceDatum> serviceList = new ArrayList<ServiceDatum>();
		while (cursor.moveToNext()) {
			ServiceDatum service = new ServiceDatum();
			service.setServiceId(Integer.parseInt(cursor.getString(svcIdIdx)));
			service.setClientId(Integer.parseInt(cursor.getString(svcIdIdx)));
			service.setChannelName(cursor.getString(chNameIdx));
			service.setImage(cursor.getString(imgIdx));
			service.setUrl(cursor.getString(urlIdx));
			serviceList.add(service);
		}
		updateChannels(serviceList);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub

	}

	/** Validating Customer balance */
	private void validateDevice() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}

		mProgressDialog = new ProgressDialog(ChannelsActivity.this,
				ProgressDialog.THEME_HOLO_DARK);
		mProgressDialog.setMessage("Connectiong to Server...");
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.setOnCancelListener(new OnCancelListener() {

			public void onCancel(DialogInterface arg0) {
				if (mProgressDialog.isShowing())
					mProgressDialog.dismiss();
				mIsReqCanceled = true;
				if (null != mExecutorService)
					if (!mExecutorService.isShutdown())
						mExecutorService.shutdownNow();
			}
		});
		mProgressDialog.show();

		String androidId = Settings.Secure.getString(getApplicationContext()
				.getContentResolver(), Settings.Secure.ANDROID_ID);
		mOBSClient.getMediaDevice(androidId, deviceCallBack);
	}

	final Callback<DeviceDatum> deviceCallBack = new Callback<DeviceDatum>() {

		@Override
		public void success(DeviceDatum device, Response arg1) {
			if (!mIsReqCanceled) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				if (device != null) {
					mApplication.setBalance(mBalance = device
							.getBalanceAmount());
					if (mBalance >= 0)
						Toast.makeText(ChannelsActivity.this,
								"Insufficient Balance.Please Make a Payment.",
								Toast.LENGTH_LONG).show();
					else {
						getServices();
					}
				}
			}
		}

		@Override
		public void failure(RetrofitError retrofitError) {
			if (!mIsReqCanceled) {
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				if (retrofitError.isNetworkError()) {
					Toast.makeText(
							ChannelsActivity.this,
							getApplicationContext().getString(
									R.string.error_network), Toast.LENGTH_LONG)
							.show();
				} else if (retrofitError.getResponse().getStatus() == 403) {
					String msg = mApplication
							.getDeveloperMessage(retrofitError);
					msg = (msg != null && msg.length() > 0 ? msg
							: "Internal Server Error");
					Toast.makeText(ChannelsActivity.this, msg,
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(
							ChannelsActivity.this,
							"Server Error : "
									+ retrofitError.getResponse().getStatus(),
							Toast.LENGTH_LONG).show();
				}
			}
		}
	};

}
