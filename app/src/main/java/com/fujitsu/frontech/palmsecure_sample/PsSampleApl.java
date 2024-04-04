/*
 * PsSampleApl.java
 *
 *	All Rights Reserved, Copyright(c) FUJITSU FRONTECH LIMITED 2021
 */

package com.fujitsu.frontech.palmsecure_sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

public class PsSampleApl extends Activity {

	private static final String TAG = "PsSampleApl";
	private static final String KEY = "PsMainFrame";
	private PsMainFrame mPsMainFrame = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "new instance");
			mPsMainFrame = new PsMainFrame(this);
		}
	}

	@Override
	protected void onDestroy() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "onDestroy");
		super.onDestroy();
		if (mPsMainFrame != null)
			mPsMainFrame.Ps_Sample_Apl_Java_onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(outState);
		outState.putParcelable(KEY, mPsMainFrame);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);
		mPsMainFrame = (PsMainFrame) savedInstanceState.getParcelable(KEY);
		mPsMainFrame.Ps_Sample_Apl_Java(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return false;
	}
}
