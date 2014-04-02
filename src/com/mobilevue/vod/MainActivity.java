package com.mobilevue.vod;

import android.app.Activity;
import android.app.AlertDialog;
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

import com.mobilevue.adapter.MainMenuAdapter;
import com.mobilevue.adapter.MyFragmentPagerAdapter;

public class MainActivity extends Activity {

	public final static String PREFS_FILE = "PREFS_FILE";
	private static final String TAG = MainActivity.class.getName();
	MyFragmentPagerAdapter mAdapter;
	ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG, "onCreate");

		setContentView(R.layout.activity_main);
		listView = (ListView) findViewById(R.id.a_main_lv_menu);
		MainMenuAdapter menuAdapter = new MainMenuAdapter(this);
		listView.setAdapter(menuAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				switch (arg2) {
				case 0:
					startActivity(new Intent(MainActivity.this,
							ChannelsActivity.class));
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
			AlertDialog mConfirmDialog = ((MyApplication) getApplicationContext())
					.getConfirmDialog(this);
			mConfirmDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			mConfirmDialog.show();
		}
		return super.onKeyDown(keyCode, event);
	}
}