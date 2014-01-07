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
import com.mobilevue.data.EpgData;
import com.mobilevue.data.ProgramGuideData;
import com.mobilevue.data.ResponseObj;
import com.mobilevue.utils.EPGDetailsAdapter;
import com.mobilevue.utils.Utilities;
import com.mobilevue.vod.R;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class EpgFragment extends Fragment {

	private static final String TAG = "EpgFragment";
	public final static String PREFS_FILE = "PREFS_FILE";
	private final static String NETWORK_ERROR = "NETWORK_ERROR";
	private ProgressDialog mProgressDialog;
	private SharedPreferences mPrefs;
	public static final String ARG_SECTION_DATE = "section_date";
	List<ProgramGuideData> progGuideList;
	ListView list;
	EPGDetailsAdapter adapter;
	String reqestedDate = null;
	boolean D;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_epg,
				container, false);
		D = ((MyApplication) getActivity().getApplicationContext()).D;
		mPrefs = getActivity().getSharedPreferences(PREFS_FILE, 0);
		list = (ListView) rootView.findViewById(R.id.fr_epg_lv_epg_dtls);
		list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		String channelName = mPrefs.getString(IPTVActivity.CHANNEL_EPG, "");
		reqestedDate = getArguments().getString(EpgFragment.ARG_SECTION_DATE);
		EpgReqDetails reqDetails = new EpgReqDetails(rootView, reqestedDate,
				channelName);
		getEpgDetails(reqDetails);
		getActivity().findViewById(R.id.a_iptv_rl_root_layout).setVisibility(
				View.VISIBLE);
		// String result =
		// "{ \"epgData\": [ { \"channelName\": \"ch1\", \"channelIcon\": \"No Icon\", \"programDate\": \"2013-10-09\", \"startTime\": \"12:00:00\", \"stopTime\": \"12:30:00\", \"programTitle\": \"Welcome To Movistar\", \"programDescription\": \"A welcome introduction to the movie channel tagged 'Movistar'\", \"type\": \"Infotainment\", \"genre\": \"\" }, { \"channelName\": \"ch1\", \"channelIcon\": \"No Icon\", \"programDate\": \"2013-10-09\", \"startTime\": \"12:30:00\", \"stopTime\": \"02:00:00\", \"programTitle\": \"Welcome To Movistar\", \"programDescription\": \"A welcome introduction to the movie channel tagged 'Movistar'\", \"type\": \"Infotainment\", \"genre\": \"\" } ] }";
		// updateDetails(result, rootView);
		return rootView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	public void getEpgDetails(EpgReqDetails rd) {
		if (D)
			Log.d(TAG, "getDetails");
		try {
			new GetEpgDetailsTask().execute(rd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class GetEpgDetailsTask extends
			AsyncTask<EpgReqDetails, Void, ResponseObj> {
		EpgReqDetails reqDetails;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (D)
				Log.d(TAG, "onPreExecute");
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			mProgressDialog = new ProgressDialog(getActivity(),
					ProgressDialog.THEME_HOLO_DARK);
			mProgressDialog.setMessage("Retrieving Details...");
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
		protected ResponseObj doInBackground(EpgReqDetails... params) {
			if (D)
				Log.d(TAG, "doInBackground");
			reqDetails = (EpgReqDetails) params[0];
			ResponseObj resObj = new ResponseObj();

			if (Utilities.isNetworkAvailable(getActivity()
					.getApplicationContext())) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("TagURL", "epgprogramguide/" + reqDetails.channelName
						+ "/" + reqDetails.date);// + "/2013-12-04");

				resObj = Utilities.callExternalApiGetMethod(getActivity()
						.getApplicationContext(), map);
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
			if (resObj.getStatusCode() == 200) {
				if (D)
					Log.d(TAG, resObj.getsResponse());
				updateDetails(resObj.getsResponse(), reqDetails.rootview);
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}

			} else {
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				Toast.makeText(getActivity(), resObj.getsErrorMessage(),
						Toast.LENGTH_LONG).show();

			}
		}

	}

	public void updateDetails(String result, View rootview) {
		if (D)
			Log.d(TAG, "updateDetails" + result);

		if (result != null) {
			progGuideList = getEPGDetailsFromJson(result);
			adapter = new EPGDetailsAdapter(getActivity(), progGuideList);
			list.setAdapter(adapter);

			list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					((AbsListView) arg0).setItemChecked(arg2, true);
					((AbsListView) arg0).smoothScrollToPosition(arg2);
					ProgramGuideData data = progGuideList.get(arg2);
					TextView chName = (TextView) getActivity().findViewById(
							R.id.a_iptv_tv_ch_name);
					TextView progName = (TextView) getActivity().findViewById(
							R.id.a_iptv_tv_Prog_name);
					TextView stTime = (TextView) getActivity().findViewById(
							R.id.a_iptv_tv_prog_start_time);
					TextView endTime = (TextView) getActivity().findViewById(
							R.id.a_iptv_tv_prog_end_time);
					TextView progDescr = (TextView) getActivity().findViewById(
							R.id.a_iptv_tv_prog_desc);
					chName.setText(data.getChannelName());
					progName.setText(data.getProgramTitle());
					SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
					Date sTime = null, eTime = null;
					try {
						sTime = tf.parse(data.getStartTime());
						eTime = tf.parse(data.getStopTime());
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					stTime.setText("Start Time: " + tf.format(sTime));
					endTime.setText("End Time  : " + tf.format(eTime));
					progDescr.setText(data.getProgramDescription());

					Button btn = (Button) getActivity().findViewById(
							R.id.a_iptv_btn_watch_remind);
					if (isCurrentProgramme(data))
						btn.setText(R.string.watch);
					else
						btn.setText(R.string.remind_me);
				}
			});
			if (progGuideList != null) {
				SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd",
						new Locale("en"));
				Calendar c = Calendar.getInstance();
				String date = df1.format(c.getTime());
				Date d1 = null, d2 = null;
				try {
					d1 = df1.parse(reqestedDate);
					d2 = df1.parse(date);

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (d1.compareTo(d2) == 0) {

					for (int i = 0; i < progGuideList.size(); i++) {

						ProgramGuideData data = progGuideList.get(i);
						if (isCurrentProgramme(data)) {
							list.setItemChecked(i, true);
							list.smoothScrollToPosition(i);
							TextView chName = (TextView) getActivity()
									.findViewById(R.id.a_iptv_tv_ch_name);
							TextView progName = (TextView) getActivity()
									.findViewById(R.id.a_iptv_tv_Prog_name);
							TextView stTime = (TextView) getActivity()
									.findViewById(
											R.id.a_iptv_tv_prog_start_time);
							TextView endTime = (TextView) getActivity()
									.findViewById(R.id.a_iptv_tv_prog_end_time);
							TextView progDescr = (TextView) getActivity()
									.findViewById(R.id.a_iptv_tv_prog_desc);
							Button btn = (Button) getActivity().findViewById(
									R.id.a_iptv_btn_watch_remind);
							btn.setText("  Watch  ");
							chName.setText(data.getChannelName());
							progName.setText(data.getProgramTitle());
							SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
							Date sTime = null, eTime = null;
							try {
								sTime = tf.parse(data.getStartTime());
								eTime = tf.parse(data.getStopTime());
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							stTime.setText("Start Time: " + tf.format(sTime));
							endTime.setText("End Time  : " + tf.format(eTime));
							progDescr.setText(data.getProgramDescription());
						}
					}
				}

			}
		}

	}

	private boolean isCurrentProgramme(ProgramGuideData data) {

		String progStartTime = data.getStartTime();
		String progStopTime = data.getStopTime();
		SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
		Date time1 = null, time2 = null;
		Date time3 = new Date();
		String t3 = tf.format(time3);

		try {
			time1 = tf.parse(progStartTime);
			time2 = tf.parse(progStopTime);
			if (time1.compareTo(time2) > 0)
				time2 = tf.parse("24:00:00");
			time3 = tf.parse(t3);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if ((time3.compareTo(time1) > 0 || time3.compareTo(time1) == 0)
				&& (time3.compareTo(time2) < 0 || time3.compareTo(time2) == 0)) {
			return true;
		}
		return false;
	}

	private List<ProgramGuideData> getEPGDetailsFromJson(String jsonText) {
		if (D)
			Log.d("getEPGDetailsFromJson", "result is " + jsonText);
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

	public class EpgReqDetails {
		public View rootview;
		public String date;
		public String channelName;

		public EpgReqDetails(View v, String date, String channelName) {
			this.rootview = v;
			this.date = date;
			this.channelName = channelName;
		}
	}

}
