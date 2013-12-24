package com.mobilevue.vod;

import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilevue.data.EpgData;
import com.mobilevue.data.ProgramGuideData;
import com.mobilevue.data.ResponseObj;
import com.mobilevue.utils.EPGDetailsAdapter;
import com.mobilevue.utils.Utilities;
import com.mobilevue.vod.R;

public class EpgDetailsActivity extends Activity {
	public static String TAG = "EpgDetailsActivity";
	private final static String NETWORK_ERROR = "Network error.";
	private ProgressDialog mProgressDialog;
	TableLayout t1;
	boolean isListHasEPGDetails = false;
	String jsonEPGResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_epg_details);
		if (savedInstanceState != null) {
			isListHasEPGDetails = savedInstanceState
					.getBoolean("isListHasEPGDetails");
			jsonEPGResult = savedInstanceState.getString("jsonEPGResult");
		}
		Intent i = getIntent();
		Bundle extras = i.getExtras();
		String title = extras.getString(IPTVActivity.KEY_TITLE);
		TextView tv = (TextView) findViewById(R.id.a_epg_tv_prog_guide);
		tv.setText(title);
		if (!isListHasEPGDetails) {
			getEpgDetails();
		} else {
			List<ProgramGuideData> progGuideList = getEPGDetailsFromJson(jsonEPGResult);
			buildRowList(progGuideList);
		}
	}

	private void getEpgDetails() {
		// TODO Auto-generated method stub
		new getEpgDetailsAsyncTask().execute();
	}

	private class getEpgDetailsAsyncTask extends
			AsyncTask<String, Void, ResponseObj> {

		protected void onPreExecute() {
			super.onPreExecute();

			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			mProgressDialog = new ProgressDialog(EpgDetailsActivity.this,
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
		protected ResponseObj doInBackground(String... arg0) {
			Log.d(TAG, "doInBackground");
			ResponseObj resObj = new ResponseObj();

			{
				if (Utilities.isNetworkAvailable(getApplicationContext())) {
					HashMap<String, String> map = new HashMap<String, String>();
					Intent i = getIntent();
					Bundle extras = i.getExtras();
					String title = extras.getString(IPTVActivity.KEY_TITLE);
					map.put("TagURL", "epgprogramguide/" + title + "/0");
					resObj = Utilities.callExternalApiGetMethod(
							getApplicationContext(), map);
				} else {
					resObj.setFailResponse(100, NETWORK_ERROR);
				}
			}
			return resObj;
		}

		protected void onPostExecute(ResponseObj resObj) {
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			if (resObj.getStatusCode() == 200) {
				Log.d("EPGAct-GetEPGDetails", resObj.getsResponse());
				jsonEPGResult = resObj.getsResponse();
				isListHasEPGDetails = true;
				List<ProgramGuideData> progGuideList = getEPGDetailsFromJson(resObj
						.getsResponse());
				buildRowList(progGuideList);

			} else {
				Toast.makeText(EpgDetailsActivity.this,
						resObj.getsErrorMessage(), Toast.LENGTH_LONG).show();

			}
		}
	}

	private List<ProgramGuideData> getEPGDetailsFromJson(String jsonText) {
		Log.i("getEPGDetailsFromJson", "result is \r\n" + jsonText);
		EpgData response = null;
		try {
			ObjectMapper mapper = new ObjectMapper().setVisibility(
					JsonMethod.FIELD, Visibility.ANY);
			mapper.configure(
					DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);
			response = mapper.readValue(jsonText, EpgData.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response.getEpgData();
	}

	private void buildRowList(List<ProgramGuideData> result) {
		ListView listview = (ListView) findViewById(R.id.a_epg_lv_prog_details);
		EPGDetailsAdapter adapter = new EPGDetailsAdapter(this, result);
		listview.setAdapter(adapter);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putBoolean("isListHasEPGDetails", isListHasEPGDetails);
		outState.putString("jsonEPGResult", jsonEPGResult);
	}

}