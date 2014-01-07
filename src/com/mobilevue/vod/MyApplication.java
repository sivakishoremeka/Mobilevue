package com.mobilevue.vod;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(formKey = "", // will not be used
mailTo = "kishoremekas@gmail.com", // my email here
mode = ReportingInteractionMode.TOAST, resToastText = R.string.crash_toast_text)
public class MyApplication extends Application {
	public boolean D = false;
	@Override
	public void onCreate() {
		super.onCreate();

		// The following line triggers the initialization of ACRA
		ACRA.init(this);
	}
}
