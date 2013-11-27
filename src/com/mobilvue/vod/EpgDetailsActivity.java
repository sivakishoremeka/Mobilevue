package com.mobilvue.vod;

import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilevue.vod.R;
import com.mobilvue.data.EpgData;
import com.mobilvue.data.ProgramGuideData;
import com.mobilvue.data.ResponseObj;
import com.mobilvue.utils.Utilities;

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
		TableLayout tl = (TableLayout) findViewById(R.id.main_table);

		TableRow tr_head = new TableRow(this);
		tr_head.setId(10);
		tr_head.setBackgroundColor(Color.GRAY);
		tr_head.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

		TextView program = new TextView(this);
		program.setId(20);
		program.setText("Program Title");
		program.setTextColor(Color.WHITE);
		program.setPadding(5, 5, 5, 5);
		tr_head.addView(program);// add the column to the table row here

		TextView starttime = new TextView(this);
		starttime.setId(21);// define id that must be unique
		starttime.setText("Start Time"); // set the text for the header
		starttime.setTextColor(Color.WHITE); // set the color
		starttime.setPadding(5, 5, 5, 5); // set the padding (if required)
		tr_head.addView(starttime); // add the column to the table row here

		TextView endtime = new TextView(this);
		endtime.setId(22);// define id that must be unique
		endtime.setText("End Time"); // set the text for the header
		endtime.setTextColor(Color.WHITE); // set the color
		endtime.setPadding(5, 5, 5, 5); // set the padding (if required)
		tr_head.addView(endtime); // add the column to the table row here

		tl.addView(tr_head, new TableLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		tl.removeAllViews();

		if (savedInstanceState != null) {
			isListHasEPGDetails = savedInstanceState
					.getBoolean("isListHasEPGDetails");
			jsonEPGResult = savedInstanceState.getString("jsonEPGResult");
		}
		if (!isListHasEPGDetails) {
			Utilities.lockScreenOrientation(getApplicationContext(),
					EpgDetailsActivity.this);
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
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
		}

		@Override
		protected ResponseObj doInBackground(String... arg0) {
			Log.d(TAG, "doInBackground");
			ResponseObj resObj = new ResponseObj();

			{
				if (Utilities.isNetworkAvailable(getApplicationContext())) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("TagURL", "epgprogramguide/ch1/0");
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
				Utilities.unlockScreenOrientation(EpgDetailsActivity.this);
			} else {
				Toast.makeText(EpgDetailsActivity.this,
						resObj.getsErrorMessage(), Toast.LENGTH_LONG).show();
				Utilities.unlockScreenOrientation(EpgDetailsActivity.this);
			}
		}
	}

	/*
	 * public List<ProgramGuideData> callEpgUrl() {
	 * 
	 * StringBuilder builder = new StringBuilder(); HttpClient client =
	 * MySSLSocketFactory.getNewHttpClient();// new // DefaultHttpClient();
	 * 
	 * String authenticateRootUrl =
	 * "https://41.75.85.206:8080/mifosng-provider/api/v1/epgprogramguide/ch1/0"
	 * ; // authenticateRootUrl = URLEncoder.encode(authenticateRootUrl, //
	 * "UTF-8");
	 * 
	 * 
	 * Date dNow = new Date( ); SimpleDateFormat ft = new SimpleDateFormat
	 * ("yyyy-MM-dd hh:mm:ss");
	 * 
	 * 
	 * SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
	 * SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
	 * 
	 * 
	 * // System.out.println("Current Date: " + ft.format(dNow)); HttpGet
	 * httpget = new HttpGet(authenticateRootUrl);
	 * 
	 * httpget.setHeader("X-Mifos-Platform-TenantId", "default");
	 * httpget.setHeader("Authorization", "Basic " +
	 * "YmlsbGluZzpiaWxsaW5nYWRtaW5AMTM="); httpget.setHeader("Content-Type",
	 * "application/json"); Log.i("callAuthenticateApi", "Calling " +
	 * httpget.getURI());
	 * 
	 * try {
	 * 
	 * Log.i("callAddClientApi", "httpget is " + httpget.toString());
	 * 
	 * HttpResponse response = client.execute(httpget); StatusLine statusLine =
	 * response.getStatusLine(); int statusCode = statusLine.getStatusCode(); if
	 * (statusCode == 200) { HttpEntity entity = response.getEntity();
	 * InputStream content = entity.getContent();
	 * 
	 * BufferedReader reader = new BufferedReader( new
	 * InputStreamReader(content)); String line; while ((line =
	 * reader.readLine()) != null) { builder.append(line); } } else {
	 * Log.e("callAuthenticateAPI", "Failed to download file"); } } catch
	 * (ClientProtocolException e) { e.printStackTrace(); } catch (IOException
	 * e) { e.printStackTrace(); }
	 * 
	 * return readJsonUser(builder.toString()); }
	 */

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

		setContentView(R.layout.activity_epg_details);

		TableLayout tl = (TableLayout) findViewById(R.id.main_table);
		tl.removeAllViews();

		// Add header colums to the table
		TableRow tr_head = new TableRow(this);
		tr_head.setId(10);
		tr_head.setBackgroundColor(Color.GRAY);
		tr_head.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

		TextView program = new TextView(this);
		program.setId(20);
		program.setText("Program Title");
		program.setTextColor(Color.WHITE);
		program.setPadding(5, 5, 5, 5);
		tr_head.addView(program);// add the column to the table row here

		TextView starttime = new TextView(this);
		starttime.setId(21);// define id that must be unique
		starttime.setText("Start Time"); // set the text for the header
		starttime.setTextColor(Color.WHITE); // set the color
		starttime.setPadding(5, 5, 5, 5); // set the padding (if required)
		tr_head.addView(starttime); // add the column to the table row here

		TextView endtime = new TextView(this);
		endtime.setId(22);// define id that must be unique
		endtime.setText("End Time"); // set the text for the header
		endtime.setTextColor(Color.WHITE); // set the color
		endtime.setPadding(5, 5, 5, 5); // set the padding (if required)
		tr_head.addView(endtime); // add the column to the table row here

		tl.addView(tr_head, new TableLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		Integer count = 0;
		// Here add the tables rows in each iteration
		for (ProgramGuideData data : result) {

			TableRow tr = new TableRow(this);
			if (count % 2 != 0)
				tr.setBackgroundColor(Color.GRAY);
			tr.setId(100 + count);
			tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT));

			TextView programCol = new TextView(this);
			programCol.setId(200 + count);
			programCol.setText(data.getChannelName());
			programCol.setPadding(2, 0, 5, 0);
			programCol.setTextColor(Color.WHITE);
			tr.addView(programCol);

			TextView starttimeCol = new TextView(this);
			starttimeCol.setId(200 + count);
			starttimeCol.setText(data.getStartTime());
			starttimeCol.setPadding(2, 0, 5, 0);
			starttimeCol.setTextColor(Color.WHITE);
			tr.addView(starttimeCol);

			TextView endtimeCol = new TextView(this);
			endtimeCol.setId(200 + count);
			endtimeCol.setText(data.getStopTime());
			endtimeCol.setPadding(2, 0, 5, 0);
			endtimeCol.setTextColor(Color.WHITE);
			tr.addView(endtimeCol);

			// finally add this to the table row
			tl.addView(tr, new TableLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			count++;
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putBoolean("isListHasEPGDetails", isListHasEPGDetails);
		outState.putString("jsonEPGResult", jsonEPGResult);
	}

}