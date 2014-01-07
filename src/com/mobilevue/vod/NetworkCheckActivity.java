package com.mobilevue.vod;

import com.mobilevue.utils.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class NetworkCheckActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		boolean isConnected = Utilities
				.isNetworkAvailable(getApplicationContext());
		if (isConnected) {
			Intent intent = new Intent(getApplicationContext(),
					AuthenticationAcitivity.class);
			startActivity(intent);
			this.finish();
		} else {
			AlertDialog dialog = new AlertDialog.Builder(
					NetworkCheckActivity.this, AlertDialog.THEME_HOLO_LIGHT)
					.create();
			dialog.setIcon(R.drawable.ic_logo_confirm_dialog);
			dialog.setTitle("Communication Error");
			dialog.setMessage("Please check your internet connection.");
			dialog.setCancelable(false);
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int buttonId) {
							NetworkCheckActivity.this.finish();
						}
					});
			dialog.show();
		}
	}

}
