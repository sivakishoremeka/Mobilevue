package com.mobilevue.vod;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilevue.data.MovieDetailsObj;
import com.mobilevue.data.ResponseObj;
import com.mobilevue.utils.Utilities;
import com.nostra13.universalimageloader.core.ImageLoader;

public class VodMovieDetailsActivity extends Activity
{

	public static String TAG = "VodMovieDetailsActivity";
	private final static String NETWORK_ERROR = "NETWORK_ERROR";
	private final static String GET_MOVIE_DETAILS = "GET_MOVIE_DETAILS";
	private final static String BOOK_ORDER = "BOOK_ORDER";
	private final static String INVALID_REQUEST = "INVALID_REQUEST";
	SurfaceView videoSurface;
	MediaPlayer player;
	VideoControllerView controller;
	private ProgressDialog mProgressDialog;
	String mediaId;
	String eventId;
	boolean D;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vod_mov_details);
		D = ((MyApplication) getApplicationContext()).D;
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		Bundle b = getIntent().getExtras();
		mediaId = b.getString("MediaId");
		eventId = b.getString("EventId");

		if ((!(mediaId.equalsIgnoreCase("")) || mediaId != null)
				&& (!(eventId.equalsIgnoreCase("")) || eventId != null)) {
			RelativeLayout rl = (RelativeLayout) findViewById(R.id.a_vod_mov_dtls_root_layout);
			rl.setVisibility(View.INVISIBLE);
			UpdateDetails();
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			break;
		default:
			break;
		}
		return true;
	}

	public void btnOnClick(View v) {
		if (D)
			Log.d("Btn Click", ((Button) v).getText().toString());
		AlertDialog dialog = new AlertDialog.Builder(
				VodMovieDetailsActivity.this, AlertDialog.THEME_HOLO_LIGHT)
				.create();
		dialog.setIcon(R.drawable.ic_logo_confirm_dialog);
		dialog.setTitle("Confirmation");
		dialog.setMessage("Do you want to continue?");
		dialog.setCancelable(false);

		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int buttonId) {
						BookOrder();
					}
				});
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int buttonId) {

					}
				});
		dialog.show();
	}

	private void BookOrder() {
		// TODO Auto-generated method stub
		new doBackGround().execute(BOOK_ORDER, "HD", "RENT");
	}

	public void UpdateDetails() {
		// if(D) Log.d(TAG, "getDetails");
		try {

			new doBackGround().execute(GET_MOVIE_DETAILS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class doBackGround extends AsyncTask<String, Void, ResponseObj> {
		private String taskName = "";

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// if(D) Log.d(TAG, "onPreExecute");
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			mProgressDialog = new ProgressDialog(VodMovieDetailsActivity.this,
					ProgressDialog.THEME_HOLO_DARK);
			mProgressDialog.setMessage("Retrieving Details...");
			mProgressDialog.setCancelable(true);
			mProgressDialog.show();
		}

		@Override
		protected ResponseObj doInBackground(String... params) {
			// if(D) Log.d(TAG, "doInBackground");
			taskName = params[0];
			ResponseObj resObj = new ResponseObj();
			if (Utilities.isNetworkAvailable(VodMovieDetailsActivity.this
					.getApplicationContext())) {
				if (taskName.equalsIgnoreCase(GET_MOVIE_DETAILS)) {

					HashMap<String, String> map = new HashMap<String, String>();
					String deviceId = Settings.Secure.getString(
							getApplicationContext().getContentResolver(),
							Settings.Secure.ANDROID_ID);
					map.put("TagURL", "assetdetails/" + mediaId);
					map.put("eventId", eventId);
					map.put("deviceId", deviceId);

					resObj = Utilities.callExternalApiGetMethod(
							VodMovieDetailsActivity.this
									.getApplicationContext(), map);
					return resObj;
				} else if (taskName.equalsIgnoreCase(BOOK_ORDER)) {
					HashMap<String, String> map = new HashMap<String, String>();
					String sDateFormat = "yyyy-mm-dd";
					DateFormat df = new SimpleDateFormat(sDateFormat);
					String formattedDate = df.format(new Date());
					String deviceId = Settings.Secure.getString(
							getApplicationContext().getContentResolver(),
							Settings.Secure.ANDROID_ID);

					map.put("TagURL", "eventorder");
					map.put("locale", "en");
					map.put("dateFormat", sDateFormat);
					map.put("eventBookedDate", formattedDate);
					map.put("formatType", params[1]);
					map.put("optType", params[2]);
					map.put("eventId", eventId);
					map.put("deviceId", deviceId);

					resObj = Utilities.callExternalApiPostMethod(
							VodMovieDetailsActivity.this
									.getApplicationContext(), map);
					return resObj;
				} else {
					resObj.setFailResponse(100, INVALID_REQUEST);
					return resObj;
				}
			} else {
				resObj.setFailResponse(100, NETWORK_ERROR);
				return resObj;
			}

		}

		@Override
		protected void onPostExecute(ResponseObj resObj) {
			super.onPostExecute(resObj);
			if (D)
				Log.d(TAG, "onPostExecute");

			if (resObj.getStatusCode() == 200) {
				if (taskName.equalsIgnoreCase(GET_MOVIE_DETAILS)) {
					updateUI(resObj.getsResponse());
					RelativeLayout rl = (RelativeLayout) findViewById(R.id.a_vod_mov_dtls_root_layout);
					rl.setVisibility(View.VISIBLE);
					if (mProgressDialog.isShowing()) {
						mProgressDialog.dismiss();
					}
				} else if (taskName.equalsIgnoreCase(BOOK_ORDER)) {
					if (mProgressDialog.isShowing()) {
						mProgressDialog.dismiss();
					}
					Intent intent = new Intent();
					try {
						intent.putExtra(
								"URL",
								((String) (new JSONObject(resObj.getsResponse()))
										.get("resourceIdentifier")));
						intent.putExtra("VIDEOTYPE", "VOD");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					((MyApplication) (VodMovieDetailsActivity.this
							.getApplicationContext())).startPlayer(intent,
							VodMovieDetailsActivity.this);
					finish();
				}

			} else {
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(
						VodMovieDetailsActivity.this,
						AlertDialog.THEME_HOLO_LIGHT);
				// Add the buttons
				builder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// MovieDetailsActivity.this.finish();
							}
						});
				AlertDialog dialog = builder.create();
				dialog.setMessage(resObj.getsErrorMessage());
				dialog.show();
			}
		}

		public void updateUI(String jsonText) {
			if (D)
				Log.d(TAG, "updateUI" + jsonText);
			if (jsonText != null) {
				MovieDetailsObj mvDtlsObj = null;
				try {
					ObjectMapper mapper = new ObjectMapper().setVisibility(
							JsonMethod.FIELD, Visibility.ANY);
					mapper.configure(
							DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
							false);
					mvDtlsObj = mapper.readValue(jsonText,
							MovieDetailsObj.class);
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(),
							"JSON Parsing Error", Toast.LENGTH_LONG).show();
					Log.i("UpdateUI Json Exception:", e.getMessage());
				}

				ImageLoader
						.getInstance()
						.displayImage(
								mvDtlsObj.getImage(),
								((ImageView) findViewById(R.id.a_vod_mov_dtls_iv_mov_img)));

				((RatingBar) findViewById(R.id.a_vod_mov_dtls_rating_bar))
						.setRating(Float.parseFloat(mvDtlsObj.getRating()));

				((TextView) findViewById(R.id.a_vod_mov_dtls_tv_mov_title))
						.setText(mvDtlsObj.getTitle());
				((TextView) findViewById(R.id.a_vod_mov_dtls_tv_descr_value))
						.setText(mvDtlsObj.getOverview());
				((TextView) findViewById(R.id.a_vod_mov_dtls_tv_durn_value))
						.setText(mvDtlsObj.getDuration());
				((TextView) findViewById(R.id.a_vod_mov_dtls_tv_lang_value))
						.setText(mvDtlsObj.getLanguage() + "");
				((TextView) findViewById(R.id.a_vod_mov_dtls_tv_release_value))
						.setText(mvDtlsObj.getReleaseDate());
				((TextView) findViewById(R.id.a_vod_mov_dtls_tv_cast_value))
						.setText(mvDtlsObj.getActors());
			}
		}

	}
}
