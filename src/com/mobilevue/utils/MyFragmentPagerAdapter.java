package com.mobilevue.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.mobilevue.vod.CategoryFragment;
import com.mobilevue.vod.VodActivity;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

	SharedPreferences mPrefs;
	Context ctx;

	public MyFragmentPagerAdapter(FragmentManager fragmentManager, Context ctx) {
		super(fragmentManager);
		this.ctx = ctx;
	}

	@Override
	public int getCount() {
		return VodActivity.ITEMS;
	}

	@Override
	public Fragment getItem(int position) {
		Bundle b = new Bundle();
		b.putInt("pageno", position);
		Fragment fragment = new CategoryFragment();
		fragment.setArguments(b);
		return fragment;
	}
}
