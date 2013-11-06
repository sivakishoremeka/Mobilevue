package com.mobilvue.vod;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {

	public TabsPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int index) {
		Bundle bundle;
		CategoryFragment fragobj;
		switch (index) {
		case 0:
			// Top Rated fragment activity
			bundle = new Bundle();
			bundle.putString("category", "RELEASE");
			fragobj = new CategoryFragment();
			fragobj.setArguments(bundle);
			return fragobj;
		case 1:
			bundle = new Bundle();
			bundle.putString("category", "RATING");
			fragobj = new CategoryFragment();
			fragobj.setArguments(bundle);
			return fragobj;
			// Games fragment activity
			// return new NewReleaseFragment("RATING");
		case 2:
			bundle = new Bundle();
			bundle.putString("category", "COMMING");
			fragobj = new CategoryFragment();
			fragobj.setArguments(bundle);
			return fragobj;
			// Movies fragment activity
			// return (new NewReleaseFragment("COMMING"));
		case 3:
			bundle = new Bundle();
			bundle.putString("category", "ALL");
			fragobj = new CategoryFragment();
			fragobj.setArguments(bundle);
			return fragobj;
			// Movies fragment activity
			// return (new NewReleaseFragment("WATCHED"));
		case 4:
			bundle = new Bundle();
			bundle.putString("category", "ALL");
			fragobj = new CategoryFragment();
			fragobj.setArguments(bundle);
			return fragobj;
			// Movies fragment activity
			// return (new NewReleaseFragment("WATCHED"));
		}

		return null;
	}

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		return 5;
	}

}
