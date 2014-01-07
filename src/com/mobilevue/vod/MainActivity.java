package com.mobilevue.vod;

import com.mobilevue.utils.MainMenuAdapter;
import com.mobilevue.utils.MyFragmentPagerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.app.Activity;
import android.app.AlertDialog;

public class MainActivity extends Activity {

	public final static String PREFS_FILE = "PREFS_FILE";
	private static final String TAG = "MainActivity";
	MyFragmentPagerAdapter mAdapter;
	ListView listView;
	boolean D;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.d(TAG, "onCreate");
		setContentView(R.layout.activity_main);
		D = ((MyApplication) getApplicationContext()).D;
		listView = (ListView) findViewById(R.id.a_main_lv_menu);
		MainMenuAdapter menuAdapter = new MainMenuAdapter(this);
		listView.setAdapter(menuAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				switch (arg2) {
				case 0:
					Intent intent = new Intent();
					intent.setClass(MainActivity.this, IPTVActivity.class);
					startActivity(intent);
					break;
				case 1:
					Intent intent1 = new Intent(MainActivity.this,
							VodActivity.class);
					startActivity(intent1);
					break;
				}

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.nav_menu, menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		searchItem.setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		default:
			break;
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			AlertDialog dialog = new AlertDialog.Builder(MainActivity.this,
					AlertDialog.THEME_HOLO_LIGHT).create();
			dialog.setIcon(R.drawable.ic_logo_confirm_dialog);
			dialog.setTitle("Confirmation");
			dialog.setMessage("Do you want to close the app?");
			dialog.setCancelable(false);

			dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int buttonId) {
							MainActivity.this.finish();
						}
					});
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int buttonId) {

						}
					});
			dialog.show();
		}
		return super.onKeyDown(keyCode, event);
	}
}