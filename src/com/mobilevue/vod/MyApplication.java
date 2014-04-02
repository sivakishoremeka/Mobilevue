package com.mobilevue.vod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.mobilevue.imagehandler.AuthImageDownloader;
import com.mobilevue.retrofit.OBSClient;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

@ReportsCrashes(formKey = "", // will not be used
mailTo = "kishoremekas@gmail.com", // my email here
mode = ReportingInteractionMode.TOAST, resToastText = R.string.crash_toast_text)
public class MyApplication extends Application {
	public static String TAG = MyApplication.class.getName();
	public static String tenentId;
	public static String basicAuth;
	public static String contentType;
	public static String API_URL;
	public static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd",
			new Locale("en"));
	public Double balance = new Double("0");
	public boolean isBalCheckReq = false;
	public boolean D = true; // need to delete this variable
	public Player player = Player.NATIVE_PLAYER;

	@Override
	public void onCreate() {
		super.onCreate();

		// The following line triggers the initialization of ACRA
		ACRA.init(this);

		/** initializing the ImageLoader instance */
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.ic_default_ch)
				.showImageForEmptyUri(R.drawable.ic_default_ch)
				.showImageOnFail(R.drawable.ic_default_ch).cacheInMemory(true)
				.cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565).build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext())
				.imageDownloader(
						new AuthImageDownloader(getApplicationContext(), 3000,
								3000)).threadPoolSize(5)
				.threadPriority(Thread.NORM_PRIORITY)
				.memoryCache(new LruMemoryCache(2 * 1024 * 1024))
				.memoryCacheSize(2 * 1024 * 1024)
				.discCacheSize(50 * 1024 * 1024).discCacheFileCount(100)
				.defaultDisplayImageOptions(defaultOptions).build();

		ImageLoader.getInstance().init(config);

		/** initilizing request headers */
		API_URL = getString(R.string.server_url);
		tenentId = getString(R.string.tenent_id);
		basicAuth = getString(R.string.basic_auth);
		contentType = getString(R.string.content_type);
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

	public boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetwork = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetwork != null && wifiNetwork.isConnected()) {
			return true;
		}
		NetworkInfo mobileNetwork = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobileNetwork != null && mobileNetwork.isConnected()) {
			return true;
		}
		NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.isConnected()) {
			return true;
		}
		return false;
	}

	public OBSClient getOBSClient(Context context,
			ExecutorService mExecutorService) {
		mExecutorService = Executors.newCachedThreadPool();
		RestAdapter restAdapter = new RestAdapter.Builder()
				.setEndpoint(API_URL)
				.setLogLevel(RestAdapter.LogLevel.FULL)
				.setClient(
						new com.mobilevue.retrofit.CustomUrlConnectionClient(
								tenentId, basicAuth, contentType)).build();
		return restAdapter.create(OBSClient.class);
	}

	public AlertDialog getConfirmDialog(final Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				((Activity) context), AlertDialog.THEME_HOLO_LIGHT);
		builder.setIcon(R.drawable.ic_logo_confirm_dialog);
		builder.setTitle("Confirmation");
		builder.setMessage("Do you want to close the app?");
		builder.setCancelable(false);
		AlertDialog dialog = builder.create();
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int buttonId) {
					}
				});
		return dialog;
	}

	public enum Player {
		NATIVE_PLAYER, MXPLAYER
	}

	public String getResponseOnSuccess(Response response) {
		StringBuffer res = new StringBuffer();
		String msg = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getBody().in()));
			while ((msg = br.readLine()) != null) {
				res.append(msg);
			}
		} catch (IOException e) {
			msg = "Internal Server Error : " + e.getMessage();
			e.printStackTrace();
		}
		if (res.length() > 0) {
			return res.toString();
		} else
			return msg;
	}

	public String getDeveloperMessage(RetrofitError retrofitError) {
		StringBuffer res = new StringBuffer();
		String msg = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					retrofitError.getResponse().getBody().in()));
			while ((msg = br.readLine()) != null) {
				res.append(msg);
			}
		} catch (IOException e) {
			msg = "Internal Server Error : " + e.getMessage();
			e.printStackTrace();
		}
		if (res.length() > 0) {
			try {
				msg = new JSONObject(res.toString()).getJSONArray("errors")
						.getJSONObject(0).getString("developerMessage");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return msg;
	}
}
