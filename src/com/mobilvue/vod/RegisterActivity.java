package com.mobilvue.vod;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import com.mobilevue.vod.R;
import com.mobilvue.data.ClientData;
import com.mobilvue.data.ClientResponseData;
import com.mobilvue.utils.ResponseObj;
import com.mobilvue.utils.Utilities;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {

	public static String TAG = "RegisterActivity";
	private final static String NETWORK_ERROR = "Network error.";
	public final static String PREFS_FILE = "PREFS_FILE";
	private SharedPreferences mPrefs;
	private Editor mPrefsEditor;
	private ProgressDialog mProgressDialog;
	Handler handler = null;
	EditText username;
	EditText emailValidate;
	int clientId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		// Button register = (Button) findViewById(R.id.Register);
		username = (EditText) findViewById(R.id.username);
		emailValidate = (EditText) findViewById(R.id.EmailId);
	}

	public void btnRegister_onClick(View v) {
		String email = emailValidate.getText().toString().trim();
		String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
		if (email.matches(emailPattern)) {
			ClientData client = new ClientData();
			client.setFullname(username.getText().toString());
			client.setEmail(emailValidate.getText().toString());
			CreateClient(client);
		}

	}

	private void CreateClient(ClientData client) {
		// TODO Auto-generated method stub
		new CreateClientAsyncTask().execute(client);
	}

	private class CreateClientAsyncTask extends
			AsyncTask<ClientData, Void, ResponseObj> {
		ClientData clientData;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			Log.d(TAG, "onPreExecute");
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			mProgressDialog = new ProgressDialog(RegisterActivity.this,
					ProgressDialog.THEME_HOLO_DARK);
			mProgressDialog.setMessage("Authenticating Details...");
			mProgressDialog.setCancelable(true);
			mProgressDialog.show();
		}

		@Override
		protected ResponseObj doInBackground(ClientData... arg0) {
			Log.d(TAG, "doInBackground");
			clientData = (ClientData) arg0[0];
			ResponseObj resObj = new ResponseObj();
			{
				if (Utilities.isNetworkAvailable(getApplicationContext())) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("TagURL", "clients");
					map.put("officeId", "1");// paymentInfo.getClientId());
					map.put("dateFormat", "dd MMMM yyyy");
					map.put("lastname", "");// paymentInfo.getPaymentDate());
					map.put("firstname", "");// paymentInfo.getPaymentCode());
					map.put("middlename", "");
					map.put("locale", "en");
					map.put("fullname", clientData.getFullname());
					map.put("externalId", "");
					map.put("clientCategory", "22");
					map.put("active", "false");
					map.put("activationDate", "");
					map.put("addressNo", "ghcv");
					map.put("street", "hyderabad");
					map.put("city", "hyderabad");
					map.put("state", "ANDHRA PRADESH");
					map.put("country", "India");
					map.put("zipCode", "436346");
					map.put("phone", "346346");
					map.put("email", clientData.getEmail());
					resObj = Utilities.callExternalApiPostMethod(
							getApplicationContext(), map);
					// Log.d("RegAct-CreateClient", resObj.getsResponse());
				} else {
					resObj.setFailResponse(100, NETWORK_ERROR);
					// return resObj;
				}
				if (resObj.getStatusCode() == 200) {
					ClientResponseData clientResData = readJsonUser(resObj
							.getsResponse());
					clientId = clientResData.getClientId();
					mPrefs = getSharedPreferences(
							AuthenticationAcitivity.PREFS_FILE, 0);
					mPrefsEditor = mPrefs.edit();
					mPrefsEditor.putInt("CLIENTID", clientId);
					mPrefsEditor.commit();
					if (Utilities.isNetworkAvailable(getApplicationContext())) {
						HashMap<String, String> map = new HashMap<String, String>();
						// String androidId = "efa4c6299";
						map.put("TagURL", "ownedhardware/" + clientId);
						map.put("itemType", "1");// paymentInfo.getClientId());
						map.put("dateFormat", "dd MMMM yyyy");
						String androidId = Settings.Secure.getString(
								getApplicationContext().getContentResolver(),
								Settings.Secure.ANDROID_ID);
						map.put("serialNumber", androidId);// paymentInfo.getPaymentDate());
						map.put("provisioningSerialNumber", "PROVISIONINGDATA");// paymentInfo.getPaymentCode());
						Calendar c = Calendar.getInstance();
						// System.out.println("Current time => " + c.getTime());
						SimpleDateFormat df = new SimpleDateFormat(
								"dd MMMM yyyy");
						String formattedDate = df.format(c.getTime());
						map.put("allocationDate", formattedDate);
						map.put("locale", "en");
						map.put("status", "");
						resObj = Utilities.callExternalApiPostMethod(
								getApplicationContext(), map);
						// Log.d("RegAct-H/w Allocan", resObj.getsResponse());
					} else {
						resObj.setFailResponse(100, NETWORK_ERROR);
						// return resObj;
					}

				}
			}

			// TODO Auto-generated method stub
			return resObj;
		}

		@Override
		protected void onPostExecute(ResponseObj resObj) {
			// TODO Auto-generated method stub
			super.onPostExecute(resObj);
			Log.d(TAG, "onPostExecute");
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			if (resObj.getStatusCode() == 200) {
				Log.d("RegAct-H/w Allocan", resObj.getsResponse());
				Intent intent = new Intent(RegisterActivity.this,
						PlanActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt("CLIENTID", clientId);
				intent.putExtras(bundle);
				// RegisterActivity.this.finish();
				startActivity(intent);
			} else
				Toast.makeText(RegisterActivity.this,
						resObj.getsErrorMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.register, menu);
		return true;
	}

	private ClientResponseData readJsonUser(String jsonText) {
		// TODO Auto-generated method stub
		Log.i("readJsonUser", "result is \r\n" + jsonText);

		ClientResponseData response = new ClientResponseData();
		try {
			ObjectMapper mapper = new ObjectMapper().setVisibility(
					JsonMethod.FIELD, Visibility.ANY);
			mapper.configure(
					DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);
			;

			response = mapper.readValue(jsonText, ClientResponseData.class);
			// System.out.println(response);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;
	}
}
