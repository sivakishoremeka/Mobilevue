package com.mobilevue.vod;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.mobilevue.imagehandler.AuthImageDownloader;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

@ReportsCrashes(formKey = "", // will not be used
mailTo = "kishoremekas@gmail.com", // my email here
mode = ReportingInteractionMode.TOAST, resToastText = R.string.crash_toast_text)
public class MyApplication extends Application {
	public Double balance = new Double("0");
	public boolean isBalCheckReq = false;
	public boolean D = true;
	public Player player = Player.NATIVE_PLAYER;

	@Override
	public void onCreate() {
		super.onCreate();

		// The following line triggers the initialization of ACRA
		ACRA.init(this);

		/** initializing the ImageLoader instance */
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true).showImageOnFail(R.drawable.ic_launcher)
				.build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext())
				.imageDownloader(
						new AuthImageDownloader(getApplicationContext(), 3000,
								3000))
				.defaultDisplayImageOptions(defaultOptions).build();
		ImageLoader.getInstance().init(config);

	}

	public void startPlayer(Intent intent, Context context) {

		switch (player) {
		case NATIVE_PLAYER:
			intent.setClass(context, VideoPlayerActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			break;
		case MXPLAYER:
			intent.setClass(context, MXPlayerActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			break;
		default:
			intent.setClass(context, VideoPlayerActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			break;
		}
	}

	public enum Player {
		NATIVE_PLAYER, MXPLAYER
	}
}
