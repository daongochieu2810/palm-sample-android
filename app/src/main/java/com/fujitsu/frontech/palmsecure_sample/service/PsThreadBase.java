/*
 * PsThreadBase.java
 *
 *	All Rights Reserved, Copyright(c) FUJITSU FRONTECH LIMITED 2021
 */

package com.fujitsu.frontech.palmsecure_sample.service;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.fujitsu.frontech.palmsecure.JAVA_uint32;
import com.fujitsu.frontech.palmsecure.PalmSecureIf;
import com.fujitsu.frontech.palmsecure_sample.BuildConfig;
import com.fujitsu.frontech.palmsecure_sample.data.PsThreadResult;

public abstract class PsThreadBase extends Thread {

	protected String TAG = "";
	protected PalmSecureIf palmsecureIf = null;
	protected JAVA_uint32 moduleHandle = null;
	protected String userID = null;
	protected PsService service = null;
	protected int numberOfRetry = 0;
	protected int sleepTime = 0;
	protected long maxResults = 0;

	protected PsThreadBase(String tag, PsService service, PalmSecureIf palmsecureIf, JAVA_uint32 moduleHandle,
			String userID, int numberOfRetry, int sleepTime, long maxResults) {

		this.TAG = tag;
		this.palmsecureIf = palmsecureIf;
		this.service = service;
		this.moduleHandle = moduleHandle;
		this.userID = userID;
		this.numberOfRetry = numberOfRetry;
		this.sleepTime = sleepTime;
		this.maxResults = maxResults;
	}

	protected void Ps_Sample_Apl_Java_NotifyWorkMessage(int processKey) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "Ps_Sample_Apl_Java_NotifyWorkMessage(1)");
		}

		if (service.mResponseMessenger != null) {
			try {
				Message response = Message.obtain(null, PsService.MSG_RESPONSE_MESSAGE);
				Bundle b = new Bundle();
				PsServiceHelper.putProcessKeyToBundle(b, processKey);
				response.setData(b);
				service.mResponseMessenger.send(response);
			} catch (RemoteException e) {
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "NotifyWorkMessage (1)", e);
				}
			}
		}
	}

	protected void Ps_Sample_Apl_Java_NotifyWorkMessage(int processKey, int count) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "Ps_Sample_Apl_Java_NotifyWorkMessage(2)");
		}

		if (service.mResponseMessenger != null) {
			try {
				Message response = Message.obtain(null, PsService.MSG_RESPONSE_MESSAGE_COUNT);
				Bundle b = new Bundle();
				PsServiceHelper.putProcessKeyToBundle(b, processKey);
				PsServiceHelper.putCountToBundle(b, count);
				response.setData(b);
				service.mResponseMessenger.send(response);
			} catch (RemoteException e) {
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "NotifyWorkMessage (2)", e);
				}
			}
		}
	}

	protected void Ps_Sample_Apl_Java_NotifyGuidance(int guidanceKey, boolean error) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "Ps_Sample_Apl_Java_NotifyGuidance");
		}

		if (service.mResponseMessenger != null) {
			try {
				Message response = Message.obtain(null, PsService.MSG_RESPONSE_GUIDANCE);
				Bundle b = new Bundle();
				PsServiceHelper.putGuidanceKeyToBundle(b, guidanceKey);
				PsServiceHelper.putGuidanceErrorToBundle(b, error);
				response.setData(b);
				service.mResponseMessenger.send(response);
			} catch (RemoteException e) {
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "NotifyGuidance", e);
				}
			}
		}
	}

	protected void Ps_Sample_Apl_Java_NotifyResult_Enroll(PsThreadResult stResult, int enrollscore) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "Ps_Sample_Apl_Java_NotifyResult_Enroll");
		}
		if (service.mResponseMessenger != null) {
			try {
				Message response = Message.obtain(null, PsService.MSG_RESPONSE_ENROLL);
				Bundle b = new Bundle();
				PsServiceHelper.putEnrollScoreToBundle(b, enrollscore);
				PsServiceHelper.putPsThreadResultToBundle(b, stResult);
				response.setData(b);
				service.mResponseMessenger.send(response);
			} catch (RemoteException e) {
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "NotifyResult_Enroll", e);
				}
			}
		}

		this.service.cancelFlg = false;

	}

	protected void Ps_Sample_Apl_Java_NotifyResult_Verify(PsThreadResult stResult) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "Ps_Sample_Apl_Java_NotifyResult_Verify");
		}

		if (service.mResponseMessenger != null) {
			try {
				Message response = Message.obtain(null, PsService.MSG_RESPONSE_VERIFY);
				Bundle b = new Bundle();
				PsServiceHelper.putPsThreadResultToBundle(b, stResult);
				response.setData(b);
				service.mResponseMessenger.send(response);
			} catch (RemoteException e) {
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "NotifyResult_Verify", e);
				}
			}
		}

		this.service.cancelFlg = false;

	}

	protected void Ps_Sample_Apl_Java_NotifyResult_Identify(PsThreadResult stResult) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "Ps_Sample_Apl_Java_NotifyResult_Identify");
		}

		if (service.mResponseMessenger != null) {
			try {
				Message response = Message.obtain(null, PsService.MSG_RESPONSE_IDENTIFY);
				Bundle b = new Bundle();
				PsServiceHelper.putPsThreadResultToBundle(b, stResult);
				response.setData(b);
				service.mResponseMessenger.send(response);
			} catch (RemoteException e) {
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "NotifyResult_Identify", e);
				}
			}
		}

		this.service.cancelFlg = false;

	}

	protected void Ps_Sample_Apl_Java_NotifyResult_Cancel(PsThreadResult stResult) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "Ps_Sample_Apl_Java_NotifyResult_Cancel");
		}

		if (service.mResponseMessenger != null) {
			try {
				Message response = Message.obtain(null, PsService.MSG_RESPONSE_CANCEL);
				Bundle b = new Bundle();
				PsServiceHelper.putPsThreadResultToBundle(b, stResult);
				response.setData(b);
				service.mResponseMessenger.send(response);
			} catch (RemoteException e) {
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "NotifyResult_Cancel", e);
				}
			}
		}
	}
}
