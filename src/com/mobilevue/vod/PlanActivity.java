package com.mobilevue.vod;

import java.text.SimpleDateFormat;
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
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.mobilevue.adapter.CustomExpandableListAdapter;
import com.mobilevue.data.PlansData;
import com.mobilevue.data.ResponseObj;
import com.mobilevue.utils.Utilities;

public class PlanActivity extends Activity {

	public static String TAG = "PlanActivity";
	private final static String NETWORK_ERROR = "Network error.";
	private ProgressDialog mProgressDialog;
	int clientId;
	boolean D;

	List<PlansData> plans;
	CustomExpandableListAdapter listAdapter;
	ExpandableListView expListView;
	public static int selectedGroupItem = -1;

	// @Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plan);
		D = ((MyApplication) getApplicationContext()).D;
		SharedPreferences mPrefs = getSharedPreferences(
				AuthenticationAcitivity.PREFS_FILE, 0);
		clientId = mPrefs.getInt("CLIENTID", 0);
		if (D)
			Log.d(TAG + "-onCreate", "CLIENTID :" + clientId);
		fetchAndBuildPlanList();
	}

	private void buildPlansList() {
		expListView = (ExpandableListView) findViewById(R.id.a_exlv_plans_services);
		listAdapter = new CustomExpandableListAdapter(this, plans);
		expListView.setAdapter(listAdapter);
	}

	/*
	 * public void radioBtn_OnClick(View v) { ((RadioButton)
	 * PlanActivity.this.findViewById(R.id.plan_list_plan_rb))
	 * .setChecked(false); ((RadioButton) v).setChecked(true); selectedGroupItem
	 * = (Integer) v.getTag(); }
	 */

	public void btnSubmit_onClick(View v) {
		if (selectedGroupItem >= 0) {

			orderPlans(plans.get(selectedGroupItem).getId());
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
				AlertDialog.THEME_HOLO_LIGHT).create();
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
		new OrderPlansAsyncTask().execute();
	}

	private class OrderPlansAsyncTask extends
			AsyncTask<Void, Void, ResponseObj> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (D)
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
		protected ResponseObj doInBackground(Void... params) {
			if (D)
				Log.d(TAG, "doInBackground");
			PlansData plan = plans.get(selectedGroupItem);
			ResponseObj resObj = new ResponseObj();
			if (Utilities.isNetworkAvailable(getApplicationContext())) {
				HashMap<String, String> map = new HashMap<String, String>();
				Date date = new Date();
				SimpleDateFormat df = new SimpleDateFormat("dd MMMM yyyy",
						new Locale("en"));
				String formattedDate = df.format(date);

				map.put("TagURL", "orders/" + clientId);
				map.put("planCode", plan.getId());
				map.put("dateFormat", "dd MMMM yyyy");
				map.put("locale", "en");
				map.put("contractPeriod", plan.getContractId());
				map.put("isNewplan", "true");
				map.put("start_date", formattedDate);
				map.put("billAlign", "true");

				// Service no.r is hardcoded.
				map.put("paytermCode", plan.getServiceData().get(0)
						.getchargeCode());
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
			if (D)
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

	public void fetchAndBuildPlanList() {
		new FetchPlansAsyncTask().execute();
	}

	private class FetchPlansAsyncTask extends
			AsyncTask<Void, Void, ResponseObj> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (D)
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
				if (D)
					Log.d("PlanAct-FetchPlans", resObj.getsResponse());
				plans = getPlansFromJson(resObj.getsResponse());
				if (plans != null)
					buildPlansList();
			} else {
				Toast.makeText(PlanActivity.this, resObj.getsErrorMessage(),
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private List<PlansData> getPlansFromJson(String jsonText) {
		if (D)
			Log.d("getPlansFromJson", "result is \r\n" + jsonText);
		List<PlansData> data = null;
		try {
			ObjectMapper mapper = new ObjectMapper().setVisibility(
					JsonMethod.FIELD, Visibility.ANY);
			mapper.configure(
					DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);
			data = mapper.readValue(jsonText,
					new TypeReference<List<PlansData>>() {
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
}