package com.mobilevue.vod;

import java.util.HashMap;
import java.util.List;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;

import com.mobilevue.data.IptvData;
import com.mobilevue.data.ResponseObj;
import com.mobilevue.utils.EPGFragmentPagerAdapter;
import com.mobilevue.utils.ImageLoader;
import com.mobilevue.utils.Utilities;
import com.mobilevue.vod.R;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.ProgressDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class IPTVActivity extends FragmentActivity {

	/** This is live/Iptv activity */

	public static String TAG = "IPTVActivity";
	private final static String NETWORK_ERROR = "Network error.";
	public final static String CHANNEL_EPG = "Channel Epg";
	private ProgressDialog mProgressDialog;
	int clientId;
	EPGFragmentPagerAdapter mEpgPagerAdapter;
	ViewPager mViewPager;
	private String mChannelURL;
	boolean D;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_iptv);
		findViewById(R.id.a_iptv_rl_root_layout).setVisibility(View.INVISIBLE);
		D = ((MyApplication) getApplicationContext()).D;
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		SharedPreferences mPrefs = getSharedPreferences(
				AuthenticationAcitivity.PREFS_FILE, 0);
		clientId = mPrefs.getInt("CLIENTID", 0);
		if (D)
			Log.d(TAG + "-onCreate", "CLIENTID :" + clientId);

		Button btn = (Button) findViewById(R.id.a_iptv_btn_watch_remind);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String label = ((Button) v).getText().toString();
				if (label.trim().equalsIgnoreCase("Watch")) {

					Intent intent = new Intent(IPTVActivity.this,
							VideoPlayerActivity.class);
					Bundle bundle = new Bundle();
					bundle.putInt("CLIENTID", clientId);
					bundle.putString("VIDEOTYPE", "LIVETV");
					bundle.putString("URL", mChannelURL);
					intent.putExtras(bundle);
					startActivity(intent);
				} else {
					Toast.makeText(IPTVActivity.this,
							"Event is created for this program",
							Toast.LENGTH_LONG).show();
				}
			}
		});

		mViewPager = (ViewPager) findViewById(R.id.a_iptv_pager);
		GetChannelsList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.nav_menu, menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		searchItem.setVisible(false);
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

	private void GetChannelsList() {
		new GetChannelsListTask().execute();
	}

	private class GetChannelsListTask extends
			AsyncTask<String, Void, ResponseObj> {
		// String task;
		protected void onPreExecute() {
			super.onPreExecute();

			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			mProgressDialog = new ProgressDialog(IPTVActivity.this,
					ProgressDialog.THEME_HOLO_DARK);
			mProgressDialog.setMessage("Retriving Detials");
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
		protected ResponseObj doInBackground(String... args) {
			ResponseObj resObj = new ResponseObj();
			if (Utilities.isNetworkAvailable(getApplicationContext())) {

				HashMap<String, String> map = new HashMap<String, String>();
				map.put("TagURL", "planservices/" + clientId
						+ "?serviceType=IPTV");
				resObj = Utilities.callExternalApiGetMethod(
						getApplicationContext(), map);
			} else {
				resObj.setFailResponse(100, NETWORK_ERROR);
			}
			return resObj;
		}

		protected void onPostExecute(ResponseObj resObj) {
			if (D)
				Log.d(TAG, "onPostExecute");

			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			if (resObj.getStatusCode() == 200) {

				if (D)
					Log.d("AuthActivity-Planlistdata", resObj.getsResponse());

				updateChannels(readJsonUserforIPTV(resObj.getsResponse()));
			} else {
				Toast.makeText(IPTVActivity.this, resObj.getsErrorMessage(),
						Toast.LENGTH_LONG).show();

			}
		}

	}

	private List<IptvData> readJsonUserforIPTV(String jsonText) {
		if (D)
			Log.d("readJsonUser", "result is \r\n" + jsonText);
		List<IptvData> response = null;
		try {
			ObjectMapper mapper = new ObjectMapper().setVisibility(
					JsonMethod.FIELD, Visibility.ANY);
			mapper.configure(
					DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);

			response = mapper.readValue(jsonText,
					new TypeReference<List<IptvData>>() {
					});
		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;

	}

	private void updateChannels(List<IptvData> result) {
		int imgno = 0;
		LinearLayout channels = (LinearLayout) findViewById(R.id.a_iptv_ll_channels);
		ImageButton button;
		for (IptvData data : result) {
			SharedPreferences mPrefs = getSharedPreferences(
					AuthenticationAcitivity.PREFS_FILE, 0);
			final Editor editor = mPrefs.edit();
			editor.putString(data.getChannelName(), data.getUrl());
			editor.commit();
			imgno += 1;
			ChannelInfo chInfo = new ChannelInfo(data.getChannelName(),
					data.getUrl());
			button = new ImageButton(this);
			button.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
			button.setId(1000 + imgno);
			button.setTag(chInfo);
			button.setFocusable(false);
			button.setFocusableInTouchMode(false);
			if (imgno == 1) {
				editor.putString(CHANNEL_EPG, data.getChannelName());
				editor.commit();
				mChannelURL = data.getUrl();
			}
			ImageLoader imgLoader = new ImageLoader(IPTVActivity.this);
			imgLoader.DisplayImage(data.getImage(), button);
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ChannelInfo info = (ChannelInfo) v.getTag();
					editor.putString("EPGChannel", info.channelName);
					editor.commit();
					mChannelURL = info.channelURL;
					mEpgPagerAdapter = new EPGFragmentPagerAdapter(
							IPTVActivity.this.getSupportFragmentManager());
					mViewPager.setAdapter(mEpgPagerAdapter);
				}
			});
			channels.addView(button);

		}
		mEpgPagerAdapter = new EPGFragmentPagerAdapter(
				this.getSupportFragmentManager());
		mViewPager.setAdapter(mEpgPagerAdapter);
	}

	private class ChannelInfo {
		private String channelName;
		private String channelURL;

		public ChannelInfo(String channelName, String channelURL) {
			this.channelName = channelName;
			this.channelURL = channelURL;
		}
	}
}
