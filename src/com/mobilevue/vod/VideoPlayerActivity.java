package com.mobilevue.vod;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class VideoPlayerActivity extends Activity implements
		SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
		MediaPlayer.OnInfoListener, MediaPlayer.OnErrorListener,
		VideoControllerView.MediaPlayerControl {

	public static String TAG = VideoPlayerActivity.class.getName();
	public static int mChannelId;
	public static Uri mUri;
	SurfaceView videoSurface;
	MediaPlayer player;
	VideoControllerView controller;
	private ProgressDialog mProgressDialog;
	private boolean isLiveController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_video_player);
		videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
		SurfaceHolder videoHolder = videoSurface.getHolder();
		videoHolder.addCallback(this);
		/*player = new MediaPlayer();
		mUri = Uri.parse(getIntent().getStringExtra("URL"));
		String videoType = getIntent().getStringExtra("VIDEOTYPE");
		if (videoType.equalsIgnoreCase("LIVETV")) {
			isLiveController = true;
			VideoControllerView.sDefaultTimeout = 3000;
			mChannelId = getIntent().getIntExtra("CHANNELID", 0);
		} else if (videoType.equalsIgnoreCase("VOD")) {
			isLiveController = false;
			VideoControllerView.sDefaultTimeout = 3000;
		}
		controller = new VideoControllerView(this, (!isLiveController));
		try {
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setVolume(1.0f, 1.0f);
			// For the Data to take from previous activity
			player.setDataSource(this, mUri);
			player.setOnPreparedListener(this);
			player.setOnInfoListener(this);
			player.setOnErrorListener(this);
		} catch (IllegalArgumentException e) {
			Log.e(TAG, e.getMessage());
		} catch (SecurityException e) {
			Log.e(TAG, e.getMessage());
		} catch (IllegalStateException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}*/
	}

	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		controller.show();
		return false;
	}

	// Implement SurfaceHolder.Callback
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if ( player != null ) 
        {
            try {
                player.setDisplay(holder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {	 
		player = new MediaPlayer();
		player.reset();
		mUri = Uri.parse(getIntent().getStringExtra("URL"));
		String videoType = getIntent().getStringExtra("VIDEOTYPE");
		if (videoType.equalsIgnoreCase("LIVETV")) {
			isLiveController = true;
			VideoControllerView.sDefaultTimeout = 3000;
			mChannelId = getIntent().getIntExtra("CHANNELID", 0);
		} else if (videoType.equalsIgnoreCase("VOD")) {
			isLiveController = false;
			VideoControllerView.sDefaultTimeout = 3000;
		}
		controller = new VideoControllerView(this, (!isLiveController));
		try {
			player.setDisplay(holder);
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setVolume(1.0f, 1.0f);
			// For the Data to take from previous activity
			player.setDataSource(this, mUri);
			player.setOnPreparedListener(this);
			player.setOnInfoListener(this);
			player.setOnErrorListener(this);
			player.prepareAsync();
		} catch (IllegalArgumentException e) {
			Log.e(TAG, e.getMessage());
		} catch (SecurityException e) {
			Log.e(TAG, e.getMessage());
		} catch (IllegalStateException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	// End SurfaceHolder.Callback
	// Implement MediaPlayer.OnPreparedListener
	@Override
	public void onPrepared(MediaPlayer mp) {
		controller.setMediaPlayer(this);
		RelativeLayout rlayout = (RelativeLayout) findViewById(R.id.video_container);
		rlayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		controller.setAnchorView(rlayout);
		controller
				.setAnchorView((RelativeLayout) findViewById(R.id.video_container));
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		mp.start();
	}

	@Override
	public void onBackPressed() {
		if (player != null) {
			if (player.isPlaying())
				player.stop();
			if (controller.isShowing())
				controller.hide();
			player.release();
			player = null;
			// finish();
		}
	}

	// End MediaPlayer.OnPreparedListener

	// Implement VideoMediaController.MediaPlayerControl
	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	@Override
	public int getBufferPercentage() {
		return 0;
	}

	@Override
	public int getCurrentPosition() {
		return player.getCurrentPosition();
	}

	@Override
	public int getDuration() {
		return player.getDuration();
	}

	@Override
	public boolean isPlaying() {
		return player.isPlaying();
	}

	@Override
	public void pause() {
		player.pause();
	}

	@Override
	public void seekTo(int i) {
		player.seekTo(i);
	}

	@Override
	public void start() {
		player.start();
	}

	@Override
	public boolean isFullScreen() {
		return false;
	}

	/*
	 * @Override public void toggleFullScreen() {
	 * 
	 * }
	 */

	// End VideoMediaController.MediaPlayerControl
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (player != null) {
				if (player.isPlaying())
					player.stop();
				if (controller.isShowing())
					controller.hide();
				player.release();
				player = null;
				finish();
			} else {
				finish();
			}
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
				|| keyCode == KeyEvent.KEYCODE_VOLUME_UP
				|| keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
			AudioManager audio = (AudioManager) getSystemService(VideoPlayerActivity.this.AUDIO_SERVICE);
			switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_UP:
				audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
						AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
						AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
				return true;
			default:
				return super.dispatchKeyEvent(event);
			}
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			controller.show();
			return true;
		} else
			super.onKeyDown(keyCode, event);
		return true;
	}

	@Override
	public void changeChannel(Uri uri, int channelId) {
		mChannelId = channelId;
		mUri = uri;
		if (!player.isPlaying())
			player.stop();
		player.reset();
		try {
			player.setDataSource(this, uri);
			player.setOnPreparedListener(this);
			player.setOnInfoListener(this);
			player.setOnErrorListener(this);
			player.prepareAsync();
		} catch (IllegalArgumentException e) {
			Log.e(TAG, e.getMessage());
		} catch (SecurityException e) {
			Log.e(TAG, e.getMessage());
		} catch (IllegalStateException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {

			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			View decorView = getWindow().getDecorView();
			int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LOW_PROFILE;
			decorView.setSystemUiVisibility(uiOptions);
		}

		RelativeLayout rlayout = (RelativeLayout) findViewById(R.id.video_container);
		rlayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {

		/*
		 * if (Build.VERSION.SDK_INT >= 17) { if (what ==
		 * MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) { if (mProgressDialog
		 * != null && mProgressDialog.isShowing()) { mProgressDialog.dismiss();
		 * mProgressDialog = null; } } } if (what ==
		 * MediaPlayer.MEDIA_INFO_BUFFERING_START) { if (mProgressDialog != null
		 * && mProgressDialog.isShowing()) { mProgressDialog.dismiss();
		 * mProgressDialog = null; } mProgressDialog = new
		 * ProgressDialog(VideoPlayerActivity.this,
		 * ProgressDialog.THEME_HOLO_DARK);
		 * mProgressDialog.setMessage("Buffering"); //
		 * mProgressDialog.setCancelable(true);
		 * mProgressDialog.setCanceledOnTouchOutside(false);
		 * mProgressDialog.setOnCancelListener(new OnCancelListener() {
		 * 
		 * public void onCancel(DialogInterface arg0) { if
		 * (mProgressDialog.isShowing()) mProgressDialog.dismiss(); finish(); }
		 * }); mProgressDialog.show(); } else if (what ==
		 * MediaPlayer.MEDIA_INFO_BUFFERING_END) { if (mProgressDialog != null
		 * && mProgressDialog.isShowing()) { mProgressDialog.dismiss();
		 * mProgressDialog = null; } }
		 *//*
			 * else if (what == MediaPlayer.MEDIA_ERROR_TIMED_OUT) { if
			 * (mProgressDialog.isShowing()) { mProgressDialog.dismiss(); } TAG,
			 * "Request timed out.Closing MediaPlayer"); finish(); }
			 */
		return true;

	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN && extra == -2147483648) {

			Toast.makeText(
					getApplicationContext(),
					"Incorrect URL or Unsupported Media Format.Media player closed.",
					Toast.LENGTH_LONG).show();
		} else if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN && extra == -1004) {

			Toast.makeText(
					getApplicationContext(),
					"Invalid Stream for this channel... Please try other channel",
					Toast.LENGTH_LONG).show();
		} else {
			controller.mHandler.removeMessages(controller.SHOW_PROGRESS);
			controller.mHandler.removeMessages(controller.FADE_OUT);
			changeChannel(mUri, mChannelId);
		}
		return true;
	}
	
	
	
	@Override
	protected void onResume() {	
		videoSurface.setVisibility(View.VISIBLE);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
        videoSurface.setVisibility(View.GONE);
		super.onPause();
	}

	@Override
	protected void onStop() {
        videoSurface.setVisibility(View.GONE);
    	if (player != null) {
			if (player.isPlaying()){
				player.stop();
				 player.release();
		           player = null;
			}
		}
		super.onStop();
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (player != null) {
			if (player.isPlaying()){
				player.stop();
				 player.release();
		           player = null;
			}
		}
		
	}
}
