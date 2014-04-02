package com.mobilevue.vod;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mobilevue.data.ActivePlanDatum;
import com.mobilevue.data.DeviceDatum;
import com.mobilevue.retrofit.OBSClient;

public class AuthenticationAcitivity extends Activity {
	public static String TAG = AuthenticationAcitivity.class.getName();
	public final static String PREFS_FILE = "PREFS_FILE";
	private SharedPreferences mPrefs;
	private Editor mPrefsEditor;
	private ProgressBar mProgressBar;
	int clientId;
	MyApplication mApplication = null;
	OBSClient mOBSClient;
	ExecutorService mExecutorService;
	boolean mIsReqCanceled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authentication);
		setTitle("");
		mApplication = ((MyApplication) getApplicationContext());
		mExecutorService = Executors.newCachedThreadPool();
		mOBSClient = mApplication.getOBSClient(this, mExecutorService);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		mProgressBar.setVisibility(View.INVISIBLE);

		validateDevice();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_btn_refresh:
			validateDevice();
			break;
		default:
			break;
		}
		return true;
	}

	private void validateDevice() {
		if (!mProgressBar.isShown()) {
			mProgressBar.setVisibility(View.VISIBLE);
		}
		String androidId = Settings.Secure.getString(getApplicationContext()
				.getContentResolver(), Settings.Secure.ANDROID_ID);
		mOBSClient.getMediaDevice(androidId, deviceCallBack);
	}

	final Callback<List<ActivePlanDatum>> activePlansCallBack = new Callback<List<ActivePlanDatum>>() {

		@Override
		public void success(List<ActivePlanDatum> list, Response arg1) {
			if (!mIsReqCanceled) {
				/** on success if client has active plans redirect to home page */
				if (list != null && list.size() > 0) {
					Intent intent = new Intent(AuthenticationAcitivity.this,
							MainActivity.class);
					AuthenticationAcitivity.this.finish();
					startActivity(intent);
				} else {
					Intent intent = new Intent(AuthenticationAcitivity.this,
							PlanActivity.class);
					AuthenticationAcitivity.this.finish();
					startActivity(intent);
				}
				AuthenticationAcitivity.this.finish();
			}
		}

		@Override
		public void failure(RetrofitError retrofitError) {
			if (!mIsReqCanceled) {
				Toast.makeText(
						AuthenticationAcitivity.this,
						"Server Error : "
								+ retrofitError.getResponse().getStatus(),
						Toast.LENGTH_LONG).show();
			}
		}
	};

	final Callback<DeviceDatum> deviceCallBack = new Callback<DeviceDatum>() {

		@Override
		public void success(DeviceDatum device, Response arg1) {
			if (!mIsReqCanceled) {
				/** on success save client id and check for active plans */
				clientId = (int) device.getClientId();
				mPrefs = getSharedPreferences(
						AuthenticationAcitivity.PREFS_FILE, 0);
				mPrefsEditor = mPrefs.edit();
				mPrefsEditor.putInt("CLIENTID", clientId);
				mPrefsEditor.commit();
				mOBSClient.getActivePlans(clientId + "", activePlansCallBack);
			}
		}

		@Override
		public void failure(RetrofitError retrofitError) {
			if (!mIsReqCanceled) {
				if (mProgressBar.isShown()) {
					mProgressBar.setVisibility(View.INVISIBLE);
				}
				if (retrofitError.isNetworkError()) {
					Toast.makeText(
							AuthenticationAcitivity.this,
							getApplicationContext().getString(
									R.string.error_network), Toast.LENGTH_LONG)
							.show();
				} else if (retrofitError.getResponse().getStatus() == 403) {
					Intent intent = new Intent(AuthenticationAcitivity.this,
							RegisterActivity.class);
					AuthenticationAcitivity.this.finish();
					startActivity(intent);
				} else if (retrofitError.getResponse().getStatus() == 401) {
					Toast.makeText(AuthenticationAcitivity.this,
							"Authorization Failed", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(
							AuthenticationAcitivity.this,
							"Server Error : "
									+ retrofitError.getResponse().getStatus(),
							Toast.LENGTH_LONG).show();
				}
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog mConfirmDialog = mApplication.getConfirmDialog(this);
			mConfirmDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (mProgressBar.isShown()) {
								mProgressBar.setVisibility(View.INVISIBLE);
							}
							mIsReqCanceled = true;
							mExecutorService.shutdownNow();
							AuthenticationAcitivity.this.finish();
						}
					});
			mConfirmDialog.show();
		}
		return super.onKeyDown(keyCode, event);
	}
}
