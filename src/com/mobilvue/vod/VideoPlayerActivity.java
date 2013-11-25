package com.mobilvue.vod;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mobilevue.vod.R;

public class VideoPlayerActivity extends Activity implements
		SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
		VideoControllerView.MediaPlayerControl {

	public static String TAG = "VideoPlayerActivity";
	SurfaceView videoSurface;
	MediaPlayer player;
	VideoControllerView controller;
	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("VideoPlayerActivity", "OnCreate");
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_video_player);
			videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
			SurfaceHolder videoHolder = videoSurface.getHolder();
			videoHolder.addCallback(this);
			// if(player!=null && !player.isPlaying()){
			player = new MediaPlayer();

			controller = new VideoControllerView(this);

			try {
				player.setAudioStreamType(AudioManager.STREAM_MUSIC);
				player.setVolume(1.0f, 1.0f);
				// For the Data to take from previous activity
				player.setDataSource(this,
						Uri.parse(getIntent().getStringExtra("URL")));
				Log.d("VideoPlayerActivity", "VideoURL:"
						+ getIntent().getStringExtra("URL"));

				
				/*player.setDataSource(this,
						Uri.parse("android.resource://" + getPackageName() +"/"+R.raw.qwe));*/
				
/*				  player.setDataSource(this, Uri.parse(
				  "http://www.wawootv.com/admin/uploads/admin/don_bishop_1_mid/don_bishop_1_mid.mp4"
				 ));
	*/			 

				// Internet Wowza server url

				// player.setDataSource(this,
				// Uri.parse("http://www.wowza.com/_h264/BigBuckBunny_115k.mov"));
                
				player.setOnPreparedListener(this);
					player.setOnInfoListener(new OnInfoListener() {					
					public boolean onInfo(MediaPlayer mp, int what, int extra) {
						if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
							if (mProgressDialog != null) {
								mProgressDialog.dismiss();
								mProgressDialog = null;
							}
							mProgressDialog = new ProgressDialog(VideoPlayerActivity.this,
									ProgressDialog.THEME_HOLO_DARK);
							mProgressDialog.setMessage("Buffering...");
							mProgressDialog.setCancelable(false);
							mProgressDialog.show();
	                    } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
	                    	if (mProgressDialog.isShowing()) {
	            				mProgressDialog.dismiss();
	            			}
	                    }
						return false;
					}
				});
				player.setOnErrorListener(new OnErrorListener() {
					
					@Override
					public boolean onError(MediaPlayer arg0, int what, int extra) {
						
						Log.d(TAG,"Media player Error is...what:"+what+" Extra:"+extra);
						if(what==MediaPlayer.MEDIA_ERROR_UNKNOWN && extra==-2147483648)
						{
							Toast.makeText(getApplicationContext(),"Incorrect URL or Unsupported Media Format.Media player closed.",Toast.LENGTH_LONG).show();
							if (player != null && player.isPlaying())
								player.stop();
							player.release();
							player = null;
							finish();
						}
						return false;
					}
				});
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}		// }
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

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d("surfaceCreated","surfaceCreated");
		player.setDisplay(holder);
		player.prepareAsync();
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		mProgressDialog = new ProgressDialog(VideoPlayerActivity.this,
				ProgressDialog.THEME_HOLO_DARK);
		mProgressDialog.setMessage("Starting MediaPlayer...");
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	// End SurfaceHolder.Callback

	// Implement MediaPlayer.OnPreparedListener
	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.d("onPrepared", "onPrepared");
		controller.setMediaPlayer(this);
		controller
				.setAnchorView((RelativeLayout) findViewById(R.id.video_container));
		if (mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		player.start();
	}

	@Override
	public void onBackPressed() {
		Log.d("onBackPressed", "onBackPressed");
		if (player != null && player.isPlaying())
			player.stop();
		player.release();
		player = null;
		// finish();
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
		Log.d("getCurrentPositon", "getCurrentPositon");
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
			Log.d("onKeyDown", "KeyCodeback");
			if (player != null && player.isPlaying()) {
				controller.hide();
				player.stop();
				player.release();
				player = null;
				finish();
			}
			else{
			finish();
			}	/*
			 * } else if (keyCode == 85) { controller.show(); if
			 * (player.isPlaying()) { player.pause(); } else { player.start(); }
			 * } else if (keyCode == 23) { controller.show(); player.pause(); }
			 * else if (keyCode == 19) { controller.show(); player.seekTo(0);
			 * player.start(); } else if (keyCode == 89) { controller.show(); if
			 * (player.getCurrentPosition() - 120000 > 0 &&
			 * (player.isPlaying())) { player.seekTo(player.getCurrentPosition()
			 * - 120000); player.start(); } } else if (keyCode == 90) {
			 * controller.show(); if (player.getCurrentPosition() + 120000 <
			 * player.getDuration() && (player.isPlaying())) {
			 * player.seekTo(player.getCurrentPosition() + 120000);
			 * player.start(); }
			 */
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
		} else
			super.onKeyDown(keyCode, event);
		return true;
	}
}
