package com.mobilevue.vod;

import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.mobilevue.data.ActivePlansData;
import com.mobilevue.data.ResponseObj;
import com.mobilevue.utils.Utilities;

public class AuthenticationAcitivity extends Activity {
	public static String TAG = "AuthenticationAcitivity";
	private final static String NETWORK_ERROR = "Network error.";
	public final static String PREFS_FILE = "PREFS_FILE";
	private SharedPreferences mPrefs;
	private Editor mPrefsEditor;
	private ProgressBar mProgressBar;
	ValidateDeviceAsyncTask mValidateDeviceTask;
	Button button;
	int clientId;
	boolean D;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authentication);
		setTitle("");
		D = ((MyApplication) getApplicationContext()).D;
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		mProgressBar.setVisibility(View.INVISIBLE);
		validateDevice();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_btn_refresh:
			validateDevice();
			break;
		default:
			break;
		}
		return true;
	}

	private void validateDevice() {
		mValidateDeviceTask = new ValidateDeviceAsyncTask();
		mValidateDeviceTask.execute();
	}

	private class ValidateDeviceAsyncTask extends
			AsyncTask<Void, Void, ResponseObj> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (D)
				if (D)
					Log.d(TAG, "onPreExecute");
			if (!mProgressBar.isShown()) {
				mProgressBar.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected ResponseObj doInBackground(Void... arg0) {
			if (D)
				Log.d(TAG, "doInBackground");
			ResponseObj resObj = new ResponseObj();
			/** authentication deviceid */
			{
				if (Utilities.isNetworkAvailable(getApplicationContext())) {
					HashMap<String, String> map = new HashMap<String, String>();
					// String androidId = "efa4c6299";
					String androidId = Settings.Secure.getString(
							getApplicationContext().getContentResolver(),
							Settings.Secure.ANDROID_ID);
					map.put("TagURL", "mediadevices/" + androidId);
					resObj = Utilities.callExternalApiGetMethod(
							getApplicationContext(), map);

					if (resObj.getStatusCode() == 200) {
						try {

							if (D)
								Log.d(TAG, resObj.getsResponse());
							JSONObject clientJson = new JSONObject(
									resObj.getsResponse());
							clientId = (Integer) (clientJson.get("clientId"));
							((MyApplication) getApplicationContext()).balance = Double
									.valueOf(clientJson.get("balanceAmount")
											.toString());
							mPrefs = getSharedPreferences(
									AuthenticationAcitivity.PREFS_FILE, 0);
							mPrefsEditor = mPrefs.edit();
							mPrefsEditor.putInt("CLIENTID", clientId);
							mPrefsEditor.commit();

							/** Calling client's plans data */
							{
								if (Utilities
										.isNetworkAvailable(getApplicationContext())) {
									map = new HashMap<String, String>();
									// String androidId = "efa4c6299";
									map.put("TagURL", "orders/" + clientId
											+ "/activeplans");
									resObj = Utilities
											.callExternalApiGetMethod(
													getApplicationContext(),
													map);
								} else {
									resObj.setFailResponse(100, NETWORK_ERROR);
									return resObj;
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					return resObj;
				} else {
					resObj.setFailResponse(100, NETWORK_ERROR);
					return resObj;
				}
			}
		}

		@Override
		protected void onPostExecute(ResponseObj resObj) {
			super.onPostExecute(resObj);
			if (D)
				Log.d(TAG, "onPostExecute");
			if (resObj.getStatusCode() == 200) {
				if (D)
					Log.d("AuthActivity-Planlistdata", resObj.getsResponse());
				List<ActivePlansData> activePlansList = readJsonUser(resObj
						.getsResponse());
				if (!activePlansList.isEmpty()) {
					Intent intent = new Intent(AuthenticationAcitivity.this,
							MainActivity.class);// IPTVActivity.class);

					AuthenticationAcitivity.this.finish();
					startActivity(intent);
				} else {
					Intent intent = new Intent(AuthenticationAcitivity.this,
							PlanActivity.class);
					AuthenticationAcitivity.this.finish();
					startActivity(intent);
				}

			} else if (resObj.getStatusCode() == 403) {
				Intent intent = new Intent(AuthenticationAcitivity.this,
						RegisterActivity.class);
				AuthenticationAcitivity.this.finish();
				startActivity(intent);
			} else {
				mProgressBar.setVisibility(View.INVISIBLE);
				mProgressBar = null;
				AlertDialog.Builder builder = new AlertDialog.Builder(
						AuthenticationAcitivity.this,
						AlertDialog.THEME_HOLO_LIGHT);
				builder.setIcon(R.drawable.ic_logo_confirm_dialog);
				builder.setTitle("Configuration Info");
				// Add the buttons
				builder.setNegativeButton("Close",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								AuthenticationAcitivity.this.finish();
							}
						});
				AlertDialog dialog = builder.create();
				dialog.setMessage(resObj.getsErrorMessage());
				dialog.show();
			}
		}
	}

	private List<ActivePlansData> readJsonUser(String jsonText) {
		if (D)
			Log.d("readJsonUser", "result is \r\n" + jsonText);
		List<ActivePlansData> data = null;
		try {
			ObjectMapper mapper = new ObjectMapper().setVisibility(
					JsonMethod.FIELD, Visibility.ANY);
			mapper.configure(
					DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);
			data = mapper.readValue(jsonText,
					new TypeReference<List<ActivePlansData>>() {
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			mValidateDeviceTask.cancel(true);
		}
		return super.onKeyDown(keyCode, event);
	}
}
