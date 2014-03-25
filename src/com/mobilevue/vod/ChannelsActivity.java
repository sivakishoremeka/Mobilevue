package com.mobilevue.vod;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
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
import com.mobilevue.data.ChannelData;
import com.mobilevue.data.ResponseObj;
import com.mobilevue.utils.Utilities;

public class ChannelsActivity extends Activity {

	private final String TAG = ChannelsActivity.this.getClass().getName();
	private final static String  GET_BAL_TASK = "Get Balance";
	private final static String  GET_CHANNELS_TASK = "Get Channels";
	private final static String NETWORK_ERROR = "Network error.";
	public final static String CHANNEL_EPG = "Channel Epg";
	public final static String PREFS_FILE = "PREFS_FILE";
	public final static String IPTV_CHANNELS_DETAILS = "IPTV Channels Details";
	public final static String CHANNELS_UPDATED_AT = "Updated At";
	public final static String CHANNELS_LIST = "Channels";
	private SharedPreferences mPrefs;
	private ProgressDialog mProgressDialog;
	private Editor mPrefsEditor;
	int clientId;
	GridView gridView;
	ChannelGridViewAdapter adapter;
	List<ChannelData> channelList = new ArrayList<ChannelData>();
	boolean D;
	boolean isLiveDataReq = false;
	boolean isBalCheckReq;
	Double bal;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channels);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		D = ((MyApplication) getApplicationContext()).D;
		isBalCheckReq = ((MyApplication) getApplicationContext()).isBalCheckReq;
		mPrefs = getSharedPreferences(AuthenticationAcitivity.PREFS_FILE, 0);
		clientId = mPrefs.getInt("CLIENTID", 0);
		if (D)
			Log.d(TAG + "-onCreate", "CLIENTID :" + clientId);
		gridView = (GridView) (findViewById(R.id.a_gv_channels));
		
		// ((MyApplication) getApplicationContext()).balance = 0.0;
		getData();
	}
	
	

	private void getData() {
		if(isBalCheckReq){
			if(isLiveDataReq)
				new GetDataAsyncTask().execute(GET_BAL_TASK);
			else if(((MyApplication) getApplicationContext()).balance>=0)
				Toast.makeText(ChannelsActivity.this, "Insufficient Balance.Please Make a Payment.", Toast.LENGTH_LONG).show();
			else 
				GetChannelsList();
		} 
		else{
			GetChannelsList();
		}
		
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.nav_menu, menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		searchItem.setVisible(false);
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
		case R.id.menu_btn_refresh:
			isLiveDataReq = true;
			if(adapter!=null){
			channelList.clear();
			adapter.notifyDataSetChanged();}
			getData();
			break;
		default:
			break;
		}
		return true;
	}

	private void GetChannelsList() {
		String ch_dtls_res = null;
		if(!isLiveDataReq){
		String	sChannelDtls = mPrefs.getString(IPTV_CHANNELS_DETAILS, "");
			if (sChannelDtls.length() != 0) {
				JSONObject json_ch_dtls = null;
				try {
					json_ch_dtls = new JSONObject(sChannelDtls);
					ch_dtls_res = json_ch_dtls.getString(CHANNELS_LIST);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				if (ch_dtls_res != null && ch_dtls_res.length() != 0) {
					String sDate = "";

					try {
						sDate = (String) json_ch_dtls.get(CHANNELS_UPDATED_AT);
						SimpleDateFormat df = new SimpleDateFormat(
								"yyyy-MM-dd", new Locale("en"));
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
							isLiveDataReq = false;
						} else {
							isLiveDataReq = true;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					isLiveDataReq = true;
				}
			} else {
				isLiveDataReq = true;
			}
		}
		if (isLiveDataReq) {
			new GetDataAsyncTask().execute(GET_CHANNELS_TASK);
		} else {
			updateChannels(ch_dtls_res);
		}
	}

	private void updateChannels(String json) {

		if (D)
			Log.d(TAG, "updateDetails" + json);
		readJsonUserforIPTV(json);
		if (channelList != null) {
			adapter = new ChannelGridViewAdapter(channelList,
					ChannelsActivity.this);
			gridView.setAdapter(adapter);
			gridView.setDrawSelectorOnTop(true);
			gridView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View imageVw,
						int position, long arg3) {
					ChannelData data = channelList.get(position);
					startActivity(new Intent(ChannelsActivity.this,
							IPTVActivity.class).putExtra(
							IPTVActivity.CHANNEL_NAME, data.getChannelName())
							.putExtra(IPTVActivity.CHANNEL_URL, data.getUrl()));
				}
			});
		}
	}

	private class GetDataAsyncTask extends
			AsyncTask<String, Void, ResponseObj> {
		String task ;
		// String task;
		protected void onPreExecute() {
			super.onPreExecute();

			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			mProgressDialog = new ProgressDialog(ChannelsActivity.this,
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
			 task= args[0];
			
			ResponseObj resObj = new ResponseObj();
			HashMap<String, String> map = new HashMap<String, String>();

			if(task == GET_BAL_TASK){
			if (Utilities.isNetworkAvailable(getApplicationContext())) {			
				// String androidId = "efa4c6299";
				String androidId = Settings.Secure.getString(
						getApplicationContext().getContentResolver(),
						Settings.Secure.ANDROID_ID);
				map.put("TagURL", "mediadevices/" + androidId);
				resObj = Utilities.callExternalApiGetMethod(
						getApplicationContext(), map);
			}
			 else {
					resObj.setFailResponse(100, NETWORK_ERROR);
				}
			}else if(task == GET_CHANNELS_TASK){
					if (Utilities.isNetworkAvailable(getApplicationContext())) {
						map = new HashMap<String, String>();
						map.put("TagURL", "planservices/" + clientId
								+ "?serviceType=IPTV");
						resObj = Utilities.callExternalApiGetMethod(
								getApplicationContext(), map);
					} else {
						resObj.setFailResponse(100, NETWORK_ERROR);
					}
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
					Log.d(TAG, resObj.getsResponse());
				if(task == GET_BAL_TASK){
				JSONObject clientJson;
				try {
					clientJson = new JSONObject(resObj.getsResponse());
					((MyApplication) getApplicationContext()).balance = Double
							.valueOf(clientJson.get("balanceAmount")
									.toString());
					if(((MyApplication) getApplicationContext()).balance>=(double)0)
						Toast.makeText(ChannelsActivity.this, "Insufficient Balance.Please Make a Payment.", Toast.LENGTH_LONG).show();
					else
						GetChannelsList();

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}
				else if(task == GET_CHANNELS_TASK){
				/**
				 * For the channels response data create JSON Object for
				 * channels in package and the updated date and save it to Prefs
				 * file with key IPTV_CHANNELS_DETAILS
				 */

				mPrefs = ChannelsActivity.this.getSharedPreferences(
						IPTVActivity.PREFS_FILE, Activity.MODE_PRIVATE);
				mPrefsEditor = mPrefs.edit();
				Date date = new Date();
				String formattedDate = Utilities.df.format(date);
				if (resObj.getsResponse().length() != 0) {
					JSONObject json = null;
					try {
						json = new JSONObject();
						json.put(CHANNELS_UPDATED_AT, formattedDate);
						json.put(CHANNELS_LIST, resObj.getsResponse());
					} catch (JSONException e) {
						e.printStackTrace();
					}
					mPrefsEditor.putString(IPTV_CHANNELS_DETAILS,
							json.toString());
					mPrefsEditor.commit();
				}

				updateChannels(resObj.getsResponse());

			} else {

				AlertDialog.Builder builder = new AlertDialog.Builder(
						ChannelsActivity.this, AlertDialog.THEME_HOLO_LIGHT);
				builder.setIcon(R.drawable.ic_logo_confirm_dialog);
				builder.setTitle("Configuration Info");
				// Add the buttons
				builder.setNegativeButton("Back",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								ChannelsActivity.this.finish();
							}
						});
				AlertDialog dialog = builder.create();
				dialog.setMessage(resObj.getsErrorMessage());
				dialog.show();
			}
		}
	}
	}
	private void readJsonUserforIPTV(String jsonText) {
		if (D)
			Log.d("readJsonUser", "result is \r\n" + jsonText);
		List<ChannelData> response = null;
		try {
			ObjectMapper mapper = new ObjectMapper().setVisibility(
					JsonMethod.FIELD, Visibility.ANY);
			mapper.configure(
					DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);

			channelList = mapper.readValue(jsonText,
					new TypeReference<List<ChannelData>>() {
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
