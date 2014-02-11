package com.mobilevue.vod;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;

import com.mobilevue.data.IptvData;
import com.mobilevue.data.Reminder;
import com.mobilevue.data.ResponseObj;
import com.mobilevue.database.DatabaseHandler;
import com.mobilevue.service.ScheduleClient;
//import com.mobilevue.utils.CenterLockHorizontalScrollview;
import com.mobilevue.utils.EPGFragmentPagerAdapter;
import com.mobilevue.utils.ImageLoader;
import com.mobilevue.utils.Utilities;
import com.mobilevue.vod.EpgFragment.ReqProgDetails;
import com.mobilevue.vod.R;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.app.ProgressDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Gravity;
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
	public final static String PREFS_FILE = "PREFS_FILE";
	public final static String IPTV_CHANNELS_DETAILS = "IPTV Channels Details";
	private SharedPreferences mPrefs;
	private Editor mPrefsEditor;
	EPGFragmentPagerAdapter mEpgPagerAdapter;
	// This is a handle so that we can call methods on our service
	private ScheduleClient scheduleClient;
	private ProgressDialog mProgressDialog;
	private String mChannelURL;
	ViewPager mViewPager;
	int clientId;
	boolean D;

	// private CenterLockHorizontalScrollview centerLockHorizontalScrollview;

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
		// centerLockHorizontalScrollview = (CenterLockHorizontalScrollview)
		// findViewById(R.id.a_iptv_hsv_channels);
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
					/**
					 * This is called to set a new notification
					 */
					ReqProgDetails progDtls = (ReqProgDetails) ((Button) v)
							.getTag();
					// Ask our service to set an alarm for that date, this
					// activity talks to the client that talks to the service

					if (progDtls != null) {
						scheduleClient.setAlarmForNotification(progDtls.c,
								progDtls.progName);
						SimpleDateFormat sdf = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm", new Locale("en"));
						String date = sdf.format(progDtls.c.getTime());

						// add to db
						DatabaseHandler dbHandler = new DatabaseHandler(
								IPTVActivity.this);
						dbHandler.deleteOldReminders();
						dbHandler.addReminder(new Reminder(progDtls.progName,
								progDtls.c.getTimeInMillis()));
						Toast.makeText(IPTVActivity.this,
								"Notification set for: " + date,
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

		mViewPager = (ViewPager) findViewById(R.id.a_iptv_pager);
		GetChannelsList();
		// Create a new service client and bind our activity to this service
		scheduleClient = new ScheduleClient(this);
		scheduleClient.doBindService();
	}

	@Override
	protected void onStop() {
		// When our activity is stopped ensure we also stop the connection to
		// the service
		// this stops us leaking our activity into the system *bad*
		if (scheduleClient != null)
			scheduleClient.doUnbindService();
		super.onStop();
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
		boolean requiredLiveData = false;
		mPrefs = this.getSharedPreferences(PREFS_FILE, Activity.MODE_PRIVATE);
		String sChannelDtls = mPrefs.getString(IPTV_CHANNELS_DETAILS, "");
		if (sChannelDtls.length() != 0) {
			JSONObject json_ch_dtls = null;
			String channel_details = null;
			try {
				json_ch_dtls = new JSONObject(mPrefs.getString(
						IPTV_CHANNELS_DETAILS, ""));
				channel_details = json_ch_dtls.getString("Response");
				// channel_details =
				// "[{\"serviceId\":2,\"clientId\":34,\"channelName\":\"BrazCom\",\"image\":\"https://spark.openbillingsystem.com/images/utv.png\",\"url\":\"http://rm-edge-4.cdn2.streamago.tv/streamagoedge/1922/815/playlist.m3u8\"},{\"serviceId\":3,\"clientId\":34,\"channelName\":\"BrazilianC\",\"image\":\"https://spark.openbillingsystem.com/images/stmv.png\",\"url\":\"http://www.wowza.com/_h264/bigbuckbunny_450.mp4\"},{\"serviceId\":4,\"clientId\":34,\"channelName\":\"Barmedas\",\"image\":\"https://spark.openbillingsystem.com/images/wb.png\",\"url\":\"http://rm-edge-4.cdn2.streamago.tv/streamagoedge/1922/815/playlist.m3u8\"},{\"serviceId\":1,\"clientId\":34,\"channelName\":\"Albanian1\",\"image\":\"https://spark.openbillingsystem.com/images/hbo.png\",\"url\":\"http://rm-edge-4.cdn2.streamago.tv/streamagoedge/1922/815/playlist.m3u8\"},{\"serviceId\":2,\"clientId\":34,\"channelName\":\"BrazCom\",\"image\":\"https:/,/spark.openbillingsystem.com/images/utv.png\",\"url\":\"http://rm-edge-4.cdn2.streamago.tv/streamagoedge/1922/815/playlist.m3u8\"},{\"serviceId\":3,\"clientId\":34,\"channelName\":\"BrazilianC\",\"image\":\"https://spark.openbillingsystem.com/images/stmv.png\",\"url\":\"http://www.wowza.com/_h264/bigbuckbunny_450.mp4\"},{\"serviceId\":4,\"clientId\":34,\"channelName\":\"Barmedas\",\"image\":\"https://spark.openbillingsystem.com/images/wb.png\",\"url\":\"http://rm-edge-4.cdn2.streamago.tv/streamagoedge/1922/815/playlist.m3u8\"},{\"serviceId\":1,\"clientId\":34,\"channelName\":\"Albanian1\",\"image\":\"https://spark.openbillingsystem.com/images/hbo.png\",\"url\":\"http://rm-edge-4.cdn2.streamago.tv/streamagoedge/1922/815/playlist.m3u8\"},{\"serviceId\":2,\"clientId\":34,\"channelName\":\"BrazCom\",\"image\":\"https://spark.openbillingsystem.com/images/utv.png\",\"url\":\"http://rm-edge-4.cdn2.streamago.tv/streamagoedge/1922/815/playlist.m3u8\"},{\"serviceId\":3,\"clientId\":34,\"channelName\":\"BrazilianC\",\"image\":\"https://spark.openbillingsystem.com/images/stmv.png\",\"url\":\"http://www.wowza.com/_h264/bigbuckbunny_450.mp4\"},{\"serviceId\":4,\"clientId\":34,\"channelName\":\"Barmedas\",\"image\":\"https://spark.openbillingsystem.com/images/wb.png\",\"url\":\"http://rm-edge-4.cdn2.streamago.tv/streamagoedge/1922/815/playlist.m3u8\"},{\"serviceId\":1,\"clientId\":34,\"channelName\":\"Albanian1\",\"image\":\"https://spark.openbillingsystem.com/images/hbo.png\",\"url\":\"http://rm-edge-4.cdn2.streamago.tv/streamagoedge/1922/815/playlist.m3u8\"},{\"serviceId\":2,\"clientId\":34,\"channelName\":\"BrazCom\",\"image\":\"https://spark.openbillingsystem.com/images/utv.png\",\"url\":\"http://rm-edge-4.cdn2.streamago.tv/streamagoedge/1922/815/playlist.m3u8\"},{\"serviceId\":3,\"clientId\":34,\"channelName\":\"BrazilianC\",\"image\":\"https://spark.openbillingsystem.com/images/stmv.png\",\"url\":\"http://www.wowza.com/_h264/bigbuckbunny_450.mp4\"},{\"serviceId\":4,\"clientId\":34,\"channelName\":\"Barmedas\",\"image\":\"https://spark.openbillingsystem.com/images/wb.png\",\"url\":\"http://rm-edge-4.cdn2.streamago.tv/streamagoedge/1922/815/playlist.m3u8\"},{\"serviceId\":1,\"clientId\":34,\"channelName\":\"Albanian1\",\"image\":\"https://spark.openbillingsystem.com/images/hbo.png\",\"url\":\"http://rm-edge-4.cdn2.streamago.tv/streamagoedge/1922/815/playlist.m3u8\"},{\"serviceId\":2,\"clientId\":34,\"channelName\":\"BrazCom\",\"image\":\"https://spark.openbillingsystem.com/images/utv.png\",\"url\":\"http://rm-edge-4.cdn2.streamago.tv/streamagoedge/1922/815/playlist.m3u8\"},{\"serviceId\":3,\"clientId\":34,\"channelName\":\"BrazilianC\",\"image\":\"https://spark.openbillingsystem.com/images/stmv.png\",\"url\":\"http://www.wowza.com/_h264/bigbuckbunny_450.mp4\"},{\"serviceId\":4,\"clientId\":34,\"channelName\":\"Barmedas\",\"image\":\"https://spark.openbillingsystem.com/images/wb.png\",\"url\":\"http://rm-edge-4.cdn2.streamago.tv/streamagoedge/1922/815/playlist.m3u8\"},{\"serviceId\":1,\"clientId\":34,\"channelName\":\"Albanian1\",\"image\":\"https://spark.openbillingsystem.com/images/hbo.png\",\"url\":\"http://rm-edge-4.cdn2.streamago.tv/streamagoedge/1922/815/playlist.m3u8\"}]";
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			if (channel_details.length() != 0) {
				String sDate = "";
				String response = "";

				try {
					sDate = (String) json_ch_dtls.get("Date");
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd",
							new Locale("en"));
					Calendar c = Calendar.getInstance();
					String currDate = df.format(c.getTime());
					Date d1 = null, d2 = null;
					try {
						d1 = df.parse(sDate);
						d2 = df.parse(currDate);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					if ((sDate.length() != 0) && (d1.compareTo(d2) == 0)) {
						updateChannels(readJsonUserforIPTV(channel_details));
					} else {
						requiredLiveData = true;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				requiredLiveData = true;
			}
		} else {
			requiredLiveData = true;
		}
		if (requiredLiveData) {
			new GetChannelsListTask().execute();
		}
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

				mPrefs = IPTVActivity.this.getSharedPreferences(
						IPTVActivity.PREFS_FILE, Activity.MODE_PRIVATE);
				mPrefsEditor = mPrefs.edit();
				Date date = new Date();
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd",
						new Locale("en"));
				String formattedDate = df.format(date);

				if (resObj.getsResponse().length() != 0) {
					JSONObject json = null;
					try {
						json = new JSONObject();
						json.put("Date", formattedDate);
						json.put("Response", resObj.getsResponse());
					} catch (JSONException e) {
						e.printStackTrace();
					}
					mPrefsEditor.putString(IPTV_CHANNELS_DETAILS,
							json.toString());
					mPrefsEditor.commit();
				}
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

		SharedPreferences mPrefs = getSharedPreferences(
				IPTVActivity.PREFS_FILE, 0);
		final Editor editor = mPrefs.edit();
		for (final IptvData data : result) {

			editor.putString(data.getChannelName(), data.getUrl());
			editor.commit();
			imgno += 1;
			ChannelInfo chInfo = new ChannelInfo(data.getChannelName(),
					data.getUrl());
			final ImageButton button = new ImageButton(this);
			LayoutParams params = new LayoutParams(Gravity.CENTER);
			params.height = 96;
			params.width = 96;
			params.setMargins(1, 1, 1, 1);
			button.setLayoutParams(params);// new LinearLayout.LayoutParams(66,
											// 66));
			button.setId(1000 + imgno);
			button.setTag(chInfo);
			button.setFocusable(false);
			button.setFocusableInTouchMode(false);
			if (imgno == 1) {
				editor.putString(CHANNEL_EPG, data.getChannelName());
				editor.commit();
				mChannelURL = data.getUrl();
			}
			final ImageLoader imgLoader = new ImageLoader(IPTVActivity.this);
			imgLoader.DisplayImage(data.getImage(), button);

			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ChannelInfo info = (ChannelInfo) v.getTag();
					editor.putString(CHANNEL_EPG, info.channelName);
					editor.commit();
					// centerLockHorizontalScrollview.setCenter(v.getId()-1001);
					mChannelURL = info.channelURL;
					mEpgPagerAdapter = new EPGFragmentPagerAdapter(
							IPTVActivity.this.getSupportFragmentManager());
					mViewPager.setAdapter(mEpgPagerAdapter);
				}
			});
			channels.addView(button);
		}
		// centerLockHorizontalScrollview.setCenter(0);
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
