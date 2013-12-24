package com.mobilevue.vod;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilevue.data.PlansData;
import com.mobilevue.data.ResponseObj;
import com.mobilevue.utils.Utilities;
import com.mobilevue.vod.R;

public class PlanActivity extends Activity {

	public static String TAG = "PlanActivity";
	private final static String NETWORK_ERROR = "Network error.";
	private ProgressDialog mProgressDialog;
	ListView listView;
	ArrayAdapter<String> adapter;
	ArrayList<HashMap<String, String>> viewList;
	String jsonPlansResult;
	boolean isListHasPlans = false;
	int clientId;

	// @Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plan);
		SharedPreferences mPrefs = getSharedPreferences(
				AuthenticationAcitivity.PREFS_FILE, 0);
		clientId = mPrefs.getInt("CLIENTID", 0);
		Log.d(TAG + "-onCreate", "CLIENTID :" + clientId);
		listView = (ListView) findViewById(R.id.a_plan_listview);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		/** We retrive the plans and bind the plans to listview */
		if (savedInstanceState != null) {
			isListHasPlans = savedInstanceState.getBoolean("isListHasPlans");
			jsonPlansResult = savedInstanceState.getString("jsonPlansResult");
		}
		if (!isListHasPlans) {

			fetchPlans();
		} else {
			List<PlansData> activePlansList = getPlansFromJson(jsonPlansResult);
			buildPlansList(activePlansList);
		}
	}

	private void buildPlansList(List<PlansData> result) {
		viewList = new ArrayList<HashMap<String, String>>();

		String[] codeArr = new String[result.size()];
		for (int i = 0; i < result.size(); i++) {
			HashMap<String, String> dataMap = new HashMap<String, String>();
			PlansData data = result.get(i);
			codeArr[i] = data.getPlanCode().toUpperCase();
			dataMap.put("id", (data.getId()) + "");
			dataMap.put("code", data.getPlanCode());
			dataMap.put("status", data.getPlanstatus().getValue());
			dataMap.put("description", data.getPlanDescription());
			viewList.add(dataMap);
		}
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice, codeArr) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView textview = (TextView) view
						.findViewById(android.R.id.text1);
				textview.setTextColor(Color.rgb(255, 124, 0));
				return view;
			}
		};
		listView.setAdapter(adapter);
	}

	public void btnSubmit_onClick(View v) {
		int count = listView.getCheckedItemCount();
		if (count > 0) {

			orderPlans(viewList.get(listView.getCheckedItemPosition())
					.get("id"));
		} else {
			Toast.makeText(getApplicationContext(), "Select a Plan",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void btnCancel_onClick(View v) {
		closeApp();
	}

	private void closeApp() {
		AlertDialog dialog = new AlertDialog.Builder(PlanActivity.this,
				AlertDialog.THEME_HOLO_DARK).create();
		dialog.setIcon(R.drawable.ic_logo_confirm_dialog);
		dialog.setTitle("Confirmation");
		dialog.setMessage("Do you want to close the app?");
		dialog.setCancelable(false);

		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int buttonId) {
						PlanActivity.this.finish();
					}
				});
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int buttonId) {

					}
				});
		dialog.show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			closeApp();
		}
		return super.onKeyDown(keyCode, event);
	}

	public void orderPlans(String planid) {
		new OrderPlansAsyncTask().execute(planid);
	}

	private class OrderPlansAsyncTask extends
			AsyncTask<String, Void, ResponseObj> {

		private String planId;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Log.d(TAG, "onPreExecute");
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			mProgressDialog = new ProgressDialog(PlanActivity.this,
					ProgressDialog.THEME_HOLO_DARK);
			mProgressDialog.setMessage("Processing Order");
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
			Log.d(TAG, "doInBackground");
			planId = params[0];
			ResponseObj resObj = new ResponseObj();
			if (Utilities.isNetworkAvailable(getApplicationContext())) {
				HashMap<String, String> map = new HashMap<String, String>();
				Date date = new Date();
				SimpleDateFormat df = new SimpleDateFormat("dd MMMM yyyy",
						new Locale("en"));
				String formattedDate = df.format(date);

				map.put("TagURL", "orders/" + clientId);
				map.put("planCode", planId);
				map.put("dateFormat", "dd MMMM yyyy");
				map.put("locale", "en");
				map.put("contractPeriod", "1");
				map.put("start_date", formattedDate);
				map.put("billAlign", "true");
				map.put("paytermCode", "monthly");

				resObj = Utilities.callExternalApiPostMethod(
						getApplicationContext(), map);
			} else {
				resObj.setFailResponse(100, NETWORK_ERROR);
			}

			return resObj;
		}

		@Override
		protected void onPostExecute(ResponseObj resObj) {
			super.onPostExecute(resObj);
			Log.d(TAG, "onPostExecute");
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			if (resObj.getStatusCode() == 200) {
				Intent intent = new Intent(PlanActivity.this,
						MainActivity.class);// IPTVActivity.class);
				PlanActivity.this.finish();
				startActivity(intent);
			} else {
				Toast.makeText(PlanActivity.this, resObj.getsErrorMessage(),
						Toast.LENGTH_LONG).show();
			}
		}
	}

	public void fetchPlans() {
		new FetchPlansAsyncTask().execute();
	}

	private class FetchPlansAsyncTask extends
			AsyncTask<Void, Void, ResponseObj> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Log.d(TAG, "onPreExecute");
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			mProgressDialog = new ProgressDialog(PlanActivity.this,
					ProgressDialog.THEME_HOLO_DARK);
			mProgressDialog.setMessage("Retrieving Plans");
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
		protected ResponseObj doInBackground(Void... arg0) {
			ResponseObj resObj = new ResponseObj();
			if (Utilities.isNetworkAvailable(getApplicationContext())) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("TagURL", "plans?planType=prepaid");
				resObj = Utilities.callExternalApiGetMethod(
						getApplicationContext(), map);
			} else {
				resObj.setFailResponse(100, NETWORK_ERROR);
			}
			return resObj;
		}

		@Override
		protected void onPostExecute(ResponseObj resObj) {
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			if (resObj.getStatusCode() == 200) {
				Log.d("PlanAct-FetchPlans", resObj.getsResponse());
				jsonPlansResult = resObj.getsResponse();
				isListHasPlans = true;
				List<PlansData> activePlansList = getPlansFromJson(resObj
						.getsResponse());
				buildPlansList(activePlansList);

			} else {
				Toast.makeText(PlanActivity.this, resObj.getsErrorMessage(),
						Toast.LENGTH_LONG).show();

			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("isListHasPlans", isListHasPlans);
		outState.putString("jsonPlansResult", jsonPlansResult);
	}

	private List<PlansData> getPlansFromJson(String jsonText) {
		Log.i("getPlansFromJson", "result is \r\n" + jsonText);
		List<PlansData> data = null;
		try {
			ObjectMapper mapper = new ObjectMapper().setVisibility(
					JsonMethod.FIELD, Visibility.ANY);
			mapper.configure(
					DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);
			;
			data = mapper.readValue(jsonText,
					new TypeReference<List<PlansData>>() {
					});
			System.out.println(data.get(0).getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

}