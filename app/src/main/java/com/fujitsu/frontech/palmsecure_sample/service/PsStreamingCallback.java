/*
 * PsStreamingCallback.java
 *
 *	All Rights Reserved, Copyright(c) FUJITSU FRONTECH LIMITED 2021
 */

package com.fujitsu.frontech.palmsecure_sample.service;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.fujitsu.frontech.palmsecure.JAVA_BioAPI_GUI_BITMAP;
import com.fujitsu.frontech.palmsecure.JAVA_BioAPI_GUI_STREAMING_CALLBACK_IF;
import com.fujitsu.frontech.palmsecure.util.PalmSecureConstant;
import com.fujitsu.frontech.palmsecure_sample.BuildConfig;
import com.fujitsu.frontech.palmsecure_sample.data.PsLogManager;

public class PsStreamingCallback implements JAVA_BioAPI_GUI_STREAMING_CALLBACK_IF {

	private static final String TAG = "PsStreamingCallback";

	@Override
	public long JAVA_BioAPI_GUI_STREAMING_CALLBACK(
			Object GuiStreamingCallbackCtx, JAVA_BioAPI_GUI_BITMAP Bitmap) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "JAVA_BioAPI_GUI_STREAMING_CALLBACK");
		}

		PsService service = (PsService) GuiStreamingCallbackCtx;
		if (service.mResponseMessenger != null) {
			try {
				Message response = android.os.Message.obtain(
						null,
						PsService.MSG_RESPONSE_SILHOUETTE);
				Bundle b = new Bundle();
				PsServiceHelper.putSilhouetteToBundle(b, Bitmap);
				response.setData(b);
				service.mResponseMessenger.send(response);
				service.silhouette = Bitmap.Bitmap.Data;
			} catch (RemoteException e) {
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "MSG_RESPONSE_SILHOUETTE", e);
				}
			}
		}

		return PalmSecureConstant.JAVA_BioAPI_OK;
	}

}
