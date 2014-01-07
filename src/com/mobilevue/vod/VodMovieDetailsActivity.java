package com.mobilevue.vod;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import com.mobilevue.data.MovieDetailsObj;
import com.mobilevue.data.ResponseObj;
import com.mobilevue.utils.Utilities;
import com.mobilevue.imagehandler.SmartImageView;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RatingBar;
import android.widget.TextView;

public class VodMovieDetailsActivity extends Activity
// implements
// SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
// VideoControllerView.MediaPlayerControl
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

		/*
		 * switch (v.getId()) { case R.id.button1: doSomething1(); break; case
		 * R.id.button2: doSomething2(); break; }
		 */
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
					Intent intent = new Intent(VodMovieDetailsActivity.this,
							VideoPlayerActivity.class);
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
					startActivity(intent);
					finish();
				}

			} else {
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(
						VodMovieDetailsActivity.this,
						AlertDialog.THEME_HOLO_DARK);
				// Add the buttons
				builder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// MovieDetailsActivity.this.finish();
							}
						});
				AlertDialog dialog = builder.create();
				dialog.setMessage(resObj.getsErrorMessage());
				/*
				 * TextView messageText =
				 * (TextView)dialog.findViewById(android.R.id.message);
				 * messageText.setGravity(Gravity.CENTER);
				 */
				dialog.show();
			}
		}

		public void updateUI(String jsonText) {
			if (D)
				Log.d(TAG, "updateUI" + jsonText);
			if (jsonText != null) {

				/*
				 * MovieDetailsObj mvDtlsObj = MovieDetailsEngine
				 * .parseMovieDetails(jsonText);
				 */

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
				((SmartImageView) findViewById(R.id.a_vod_mov_dtls_iv_mov_img))
						.setImageUrl(mvDtlsObj.getImage());

				((RatingBar) findViewById(R.id.a_vod_mov_dtls_rating_bar))
						.setRating(Float.parseFloat(mvDtlsObj.getRating()));

				((TextView) findViewById(R.id.a_vod_mov_dtls_tv_mov_title))
						.setText(mvDtlsObj.getTitle());
				((TextView) findViewById(R.id.a_vod_mov_dtls_tv_descr_value))
						.setText(mvDtlsObj.getOverview());
				// ((TextView) findViewById(R.id.mvdtls_dirby_val_tv))
				// .setText(mvDtlsObj.getDirectors());
				// ((TextView) findViewById(R.id.mvdtls_writby_val_tv))
				// .setText(mvDtlsObj.getWriters());
				// ((TextView) findViewById(R.id.mvdtls_genere_val_tv))
				// .setText(mvDtlsObj.getGenres());
				((TextView) findViewById(R.id.a_vod_mov_dtls_tv_durn_value))
						.setText(mvDtlsObj.getDuration());
				((TextView) findViewById(R.id.a_vod_mov_dtls_tv_lang_value))
						.setText(mvDtlsObj.getLanguage() + "");
				((TextView) findViewById(R.id.a_vod_mov_dtls_tv_release_value))
						.setText(mvDtlsObj.getReleaseDate());
				((TextView) findViewById(R.id.a_vod_mov_dtls_tv_cast_value))
						.setText(mvDtlsObj.getActors());
				/*
				 * if (mvDtlsObj.getArrPriceDetails().size() > 0) { for (int i =
				 * 0; i < mvDtlsObj.getArrPriceDetails().size(); i++) {
				 * PriceDetailsObj priceObj =
				 * mvDtlsObj.getArrPriceDetails().get(i); if
				 * (priceObj.getFormatType().equalsIgnoreCase("HDX")) {
				 * 
				 * Button btn = ((Button)
				 * findViewById(R.id.a_vod_mov_dtls_btn_own_hdx));
				 * btn.setText("HDX   $" + String.format("%2.2f",
				 * priceObj.getPrice()));
				 * 
				 * } else if (priceObj.getFormatType().equalsIgnoreCase( "HD"))
				 * { btn = ((Button) findViewById(R.id.mvdtls_hd_btn));
				 * btn.setVisibility(View.VISIBLE); btn.setText("HD   $" +
				 * String.format("%2.2f", priceObj.getPrice()));
				 * btn.setFocusable(true); if (!isFocusSet) {
				 * btn.setFocusableInTouchMode(true); btn.requestFocus();
				 * isFocusSet =true; }
				 * 
				 * } else if (priceObj.getFormatType().equalsIgnoreCase( "SD"))
				 * { btn = ((Button) findViewById(R.id.mvdtls_sd_btn));
				 * btn.setVisibility(View.VISIBLE); btn.setText("SD   $" +
				 * String.format("%2.2f", priceObj.getPrice()));
				 * btn.setFocusable(true); if (!isFocusSet) {
				 * btn.setFocusableInTouchMode(true); isFocusSet =true;
				 * btn.requestFocus(); }
				 * 
				 * } btn = null; } }
				 */
			}
			// prepareMediaPlayer(String promoURL);
		}

	}

	/*
	 * private void prepareMediaPlayer(String promoURL) {
	 * 
	 * videoSurface = (SurfaceView)
	 * findViewById(R.id.a_vod_mov_dtls_videoSurface); SurfaceHolder videoHolder
	 * = videoSurface.getHolder(); videoHolder.addCallback(this); player = new
	 * MediaPlayer();
	 * 
	 * VideoControllerView.sDefaultTimeout = 3000; controller = new
	 * VideoControllerView(this); try {
	 * player.setAudioStreamType(AudioManager.STREAM_MUSIC);
	 * player.setVolume(1.0f, 1.0f); player.setDataSource( this,
	 * Uri.parse(promoURL)); player.setDataSource( this,
	 * Uri.parse("http://livemobile.voicetv.co.th:1935/live/apple.sdp/playlist.m3u8"
	 * )); player.setOnPreparedListener(this); player.setOnInfoListener(new
	 * OnInfoListener() { public boolean onInfo(MediaPlayer mp, int what, int
	 * extra) { if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) { if
	 * (mProgressDialog != null) { mProgressDialog.dismiss(); mProgressDialog =
	 * null; } mProgressDialog = new ProgressDialog(
	 * VodMovieDetailsActivity.this, ProgressDialog.THEME_HOLO_DARK);
	 * mProgressDialog.setMessage("Buffering");
	 * mProgressDialog.setCanceledOnTouchOutside(false); mProgressDialog
	 * .setOnCancelListener(new OnCancelListener() {
	 * 
	 * public void onCancel(DialogInterface arg0) { if
	 * (mProgressDialog.isShowing()) mProgressDialog.dismiss(); finish(); } });
	 * mProgressDialog.show(); } else if (what ==
	 * MediaPlayer.MEDIA_INFO_BUFFERING_END) { if (mProgressDialog.isShowing())
	 * { mProgressDialog.dismiss(); } } else if (what ==
	 * MediaPlayer.MEDIA_ERROR_TIMED_OUT) { if (mProgressDialog.isShowing()) {
	 * mProgressDialog.dismiss(); } if(D) Log.d(TAG,
	 * "Request timed out.Closing MediaPlayer"); finish(); } return false; } });
	 * player.setOnErrorListener(new OnErrorListener() {
	 * 
	 * @Override public boolean onError(MediaPlayer arg0, int what, int extra) {
	 * 
	 * if(D) Log.d(TAG, "Media player Error is...what:" + what + " Extra:" +
	 * extra); if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN && extra ==
	 * -2147483648) { Toast.makeText( getApplicationContext(),
	 * "Incorrect URL or Unsupported Media Format.Media player closed.",
	 * Toast.LENGTH_LONG).show(); if (player != null && player.isPlaying())
	 * player.stop(); player.release(); player = null; finish(); } return false;
	 * } }); } catch (IllegalArgumentException e) { e.printStackTrace(); } catch
	 * (SecurityException e) { e.printStackTrace(); } catch
	 * (IllegalStateException e) { e.printStackTrace(); } catch (IOException e)
	 * { e.printStackTrace(); } catch (Exception e) { e.printStackTrace(); }
	 * 
	 * 
	 * }
	 */
	/*
	 * @Override public boolean onTouchEvent(MotionEvent event) {
	 * 
	 * controller.show(); return false; }
	 */
	/*
	 * @Override public void surfaceChanged(SurfaceHolder holder, int format,
	 * int width, int height) {
	 * 
	 * }
	 */

	/*
	 * @Override public void surfaceCreated(SurfaceHolder holder) { if(D)
	 * Log.d("surfaceCreated", "surfaceCreated"); player.setDisplay(holder);
	 * player.prepareAsync(); if (mProgressDialog != null) {
	 * mProgressDialog.dismiss(); mProgressDialog = null; } mProgressDialog =
	 * new ProgressDialog(VodMovieDetailsActivity.this,
	 * ProgressDialog.THEME_HOLO_DARK);
	 * mProgressDialog.setMessage("Starting MediaPlayer");
	 * mProgressDialog.setCanceledOnTouchOutside(false);
	 * mProgressDialog.setOnCancelListener(new OnCancelListener() {
	 * 
	 * public void onCancel(DialogInterface arg0) { if
	 * (mProgressDialog.isShowing()) mProgressDialog.dismiss(); finish(); } });
	 * mProgressDialog.show(); }
	 * 
	 * @Override public void surfaceDestroyed(SurfaceHolder holder) {
	 * 
	 * }
	 * 
	 * @Override public void onPrepared(MediaPlayer mp) { if(D)
	 * Log.d("onPrepared", "onPrepared"); controller.setMediaPlayer(this);
	 * controller .setAnchorView((RelativeLayout)
	 * findViewById(R.id.a_vod_mov_dtls_video_container)); if
	 * (mProgressDialog.isShowing()) { mProgressDialog.dismiss(); }
	 * controller.show(5000); // player.start();
	 * 
	 * }
	 * 
	 * @Override public void onBackPressed() { if(D) Log.d("onBackPressed",
	 * "onBackPressed"); if (player != null && player.isPlaying())
	 * player.stop(); player.release(); player = null; // finish(); }
	 */
	// End MediaPlayer.OnPreparedListener

	// Implement VideoMediaController.MediaPlayerControl
	/*
	 * @Override public boolean canPause() { return true; }
	 * 
	 * @Override public boolean canSeekBackward() { return true; }
	 * 
	 * @Override public boolean canSeekForward() { return true; }
	 * 
	 * @Override public int getBufferPercentage() { return 0; }
	 * 
	 * @Override public int getCurrentPosition() { return
	 * player.getCurrentPosition(); }
	 * 
	 * @Override public int getDuration() { return player.getDuration(); }
	 * 
	 * @Override public boolean isPlaying() { return player.isPlaying(); }
	 * 
	 * @Override public void pause() { player.pause(); }
	 * 
	 * @Override public void seekTo(int i) { player.seekTo(i); }
	 * 
	 * @Override public void start() { player.start(); }
	 * 
	 * @Override public boolean isFullScreen() { return false; }
	 * 
	 * 
	 * @Override public void toggleFullScreen() {
	 * 
	 * }
	 * 
	 * 
	 * // End VideoMediaController.MediaPlayerControl public boolean
	 * onKeyDown(int keyCode, KeyEvent event) { // TODO Auto-generated method
	 * stub if (keyCode == KeyEvent.KEYCODE_BACK) { if(D) Log.d("onKeyDown",
	 * "KeyCodeback"); if (player != null && player.isPlaying()) {
	 * controller.hide(); player.stop(); player.release(); player = null;
	 * finish(); } else { finish(); } } else if (keyCode ==
	 * KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
	 * keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) { AudioManager audio =
	 * (AudioManager)
	 * getSystemService(VodMovieDetailsActivity.this.AUDIO_SERVICE); switch
	 * (keyCode) { case KeyEvent.KEYCODE_VOLUME_UP:
	 * audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
	 * AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI); return true; case
	 * KeyEvent.KEYCODE_VOLUME_DOWN:
	 * audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
	 * AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI); return true;
	 * default: return super.dispatchKeyEvent(event); } } else
	 * super.onKeyDown(keyCode, event); return true; }
	 */
}
