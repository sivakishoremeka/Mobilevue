package com.mobilvue.vod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import com.mobilevue.vod.R;
import com.mobilvue.data.IptvData;
import com.mobilvue.data.ResponseObj;
import com.mobilvue.utils.IptvLazyAdapter;
import com.mobilvue.utils.Utilities;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class IPTVActivity extends Activity {

	/** This is live/Iptv activity */

	public static String TAG = "IPTVActivity";
	private final static String NETWORK_ERROR = "Network error.";
	static final String KEY_ID = "id";
	public static final String EVENT_ID = "event_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_ARTIST = "artist";
	public static final String KEY_DURATION = "duration";
	public static final String KEY_THUMB_URL = "thumb_url";
	public static final String KEY_VIDEO_URL = "video_url";

	private ProgressDialog mProgressDialog;
	//ListView listview;
	//IptvLazyAdapter iptvadapter;
	int clientId;
	String jsonIPTVResult;
	boolean isListHasChannels = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_iptv);
		if (savedInstanceState != null) {
			isListHasChannels = savedInstanceState
					.getBoolean("isListHasChannels");
			jsonIPTVResult = savedInstanceState.getString("jsonIPTVResult");
		}

		if (!isListHasChannels) {
			Utilities.lockScreenOrientation(getApplicationContext(),
					IPTVActivity.this);

			validateIptv();
		} else {
			buildIptvList(readJsonUserforIPTV(jsonIPTVResult));
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nav_menu, menu);
		return true;
	}

	private void validateIptv() {
		new ValidateIptvAsyncTask().execute();
	}

	private class ValidateIptvAsyncTask extends
			AsyncTask<Void, Void, ResponseObj> {
		protected void onPreExecute() {
			super.onPreExecute();

			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			mProgressDialog = new ProgressDialog(IPTVActivity.this,
					ProgressDialog.THEME_HOLO_DARK);
			mProgressDialog.setMessage("Retriving Detials");
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
		}

		@Override
		protected ResponseObj doInBackground(Void... args0) {
			ResponseObj resObj = new ResponseObj();
			Intent i = getIntent();
			Bundle extras = i.getExtras();
			clientId = extras.getInt("CLIENTID");
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
			Log.d(TAG, "onPostExecute");
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			if (resObj.getStatusCode() == 200) {
				Log.d("AuthActivity-Planlistdata", resObj.getsResponse());
				jsonIPTVResult = resObj.getsResponse();
				isListHasChannels = true;
				buildIptvList(readJsonUserforIPTV(resObj.getsResponse()));
				Utilities.unlockScreenOrientation(IPTVActivity.this);
			} else {
				Toast.makeText(IPTVActivity.this,
						resObj.getsErrorMessage(), Toast.LENGTH_LONG).show();
				Utilities.unlockScreenOrientation(IPTVActivity.this);
			}
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_btn_live_tv:
			// validateIptv();
			break;
		case R.id.menu_btn_vod:
			Intent i = getIntent();
			Bundle extras = i.getExtras();
			clientId = extras.getInt("CLIENTID");
			Intent intent = new Intent(IPTVActivity.this,
					VodActivity.class);
			Bundle bundle = new Bundle();
			bundle.putInt("CLIENTID", clientId);
			intent.putExtras(bundle);
			startActivity(intent);
			break;

		default:
			break;
		}

		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putBoolean("isListHasChannels", isListHasChannels);
		outState.putString("jsonIPTVResult", jsonIPTVResult);
	}

	private List<IptvData> readJsonUserforIPTV(String jsonText) {
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

	private void buildIptvList(List<IptvData> result) {
		ArrayList<HashMap<String, String>> iptvList = new ArrayList<HashMap<String, String>>();

		for (IptvData data : result) {
			HashMap<String, String> map = new HashMap<String, String>();

			map.put(KEY_TITLE, data.getChannelName());
			map.put(KEY_DURATION, null);
			map.put(KEY_THUMB_URL, data.getImage());
			map.put(KEY_VIDEO_URL, data.getUrl());
			// URL = data.getUrl();
			iptvList.add(map);
		}
		ListView list =(ListView) findViewById(R.id.a_iptv_lv_channels);
		IptvLazyAdapter iptvadapter = new IptvLazyAdapter(this, iptvList, clientId);
		list.setAdapter(iptvadapter);  
	}
}
