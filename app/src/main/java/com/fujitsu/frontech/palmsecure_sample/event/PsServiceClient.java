/*
 * PsServiceClient.java
 *
 *	All Rights Reserved, Copyright(c) FUJITSU FRONTECH LIMITED 2021
 */

package com.fujitsu.frontech.palmsecure_sample.event;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.fujitsu.frontech.palmsecure_sample.BuildConfig;
import com.fujitsu.frontech.palmsecure_sample.exception.PsAplException;
import com.fujitsu.frontech.palmsecure_sample.service.PsService;
import com.fujitsu.frontech.palmsecure_sample.service.PsServiceHelper;

public class PsServiceClient extends Handler {

	private static final String TAG = "PsServiceClient";
	private Messenger mMessenger = null;
	private ServiceConnection mCon;
	private PsBusinessListener mListener;
	private Activity mActivity;

	public PsServiceClient(PsBusinessListener listner, Activity activity)
			throws PsAplException {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "PsServiceClient");
		mListener = listner;
		mActivity = activity;
		mCon = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				if (BuildConfig.DEBUG)
					Log.d(TAG, "onServiceConnected");
				mMessenger = new Messenger(service);
				mListener.Ps_Sample_Apl_Java_NotifyService_Connected();
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				if (BuildConfig.DEBUG)
					Log.d(TAG, "onServiceDisconnected");
				mListener.Ps_Sample_Apl_Java_NotifyService_Disconnected();
			}
		};

		Intent intent = new Intent(activity, PsService.class);
		boolean bindService = activity.bindService(intent, mCon, Service.BIND_AUTO_CREATE);
		if (bindService == false) {
			PsAplException pse = new PsAplException();
			pse.setErrorMsgKey(1);
			throw pse;
		}
	}

	public void Ps_Sample_Apl_Java_ServiceDisconnect() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_ServiceDisconnect");
		mActivity.unbindService(mCon);
	}

	public void Ps_Sample_Apl_Java_ServiceRequest_InitLibrary(String aplKey, int guideMode, int dataType) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_ServiceRequest_InitLibrary: aplKey=" + aplKey
				+ " guideMode=" + guideMode + " dataType=" + dataType);
		try {
			Message request = Message
					.obtain(null, PsService.MSG_REQUEST_INIT_LIBRARY);
			Bundle b = new Bundle();
			PsServiceHelper.putApplicationKeyToBundle(b, aplKey);
			PsServiceHelper.putGuideModeToBundle(b, guideMode);
			PsServiceHelper.pubDataTypeToBundle(b, dataType);
			request.setData(b);
			request.replyTo = new Messenger(this);
			mMessenger.send(request);
		} catch (RemoteException e) {
			if (BuildConfig.DEBUG)
				Log.e(TAG, "Request InitLibrary", e);
		}
	}

	public void Ps_Sample_Apl_Java_ServiceRequest_TermLibrary() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_ServiceRequest_TermLibrary");
		try {
			Message request = Message
					.obtain(null, PsService.MSG_REQUEST_TERM_LIBRARY);
			request.replyTo = new Messenger(this);
			mMessenger.send(request);
		} catch (RemoteException e) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "Request TermLibrary", e);
			}
		}
	}

	public void Ps_Sample_Apl_Java_ServiceRequest_Enroll(String userId, int numOfRetry, int sleepTime) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_ServiceRequest_Enroll");
		try {
			Message request = Message
					.obtain(null, PsService.MSG_REQUEST_ENROLL);
			Bundle b = new Bundle();
			PsServiceHelper.putUserIdToBundle(b, userId);
			PsServiceHelper.putNumberOfRetryToBundle(b, numOfRetry);
			PsServiceHelper.putSleepTimeToBundle(b, sleepTime);
			request.setData(b);
			request.replyTo = new Messenger(this);
			mMessenger.send(request);
		} catch (RemoteException e) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "Request ENROLL", e);
			}
		}
	}

	public void Ps_Sample_Apl_Java_ServiceRequest_Verify(String userId, int numOfRetry, int sleepTime) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_ServiceRequest_Verify");
		try {
			Message request = Message
					.obtain(null, PsService.MSG_REQUEST_VERIFY);
			Bundle b = new Bundle();
			PsServiceHelper.putUserIdToBundle(b, userId);
			PsServiceHelper.putNumberOfRetryToBundle(b, numOfRetry);
			PsServiceHelper.putSleepTimeToBundle(b, sleepTime);
			request.setData(b);
			request.replyTo = new Messenger(this);
			mMessenger.send(request);
		} catch (RemoteException e) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "Request VERIFY", e);
			}
		}
	}

	public void Ps_Sample_Apl_Java_ServiceRequest_Identify(int numOfRetry, int sleepTime, int maxResults) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_ServiceRequest_Identify");
		try {
			Message request = Message.obtain(null,
					PsService.MSG_REQUEST_IDENTIFY);
			Bundle b = new Bundle();
			PsServiceHelper.putNumberOfRetryToBundle(b, numOfRetry);
			PsServiceHelper.putSleepTimeToBundle(b, sleepTime);
			PsServiceHelper.putMaxResultsToBundle(b, maxResults);
			request.setData(b);
			request.replyTo = new Messenger(this);
			mMessenger.send(request);
		} catch (RemoteException e) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "Request IDENTIFY", e);
			}
		}
	}

	public void Ps_Sample_Apl_Java_ServiceRequest_Cancel() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_ServiceRequest_Cancel");
		try {
			Message request = Message
					.obtain(null, PsService.MSG_REQUEST_CANCEL);
			request.replyTo = new Messenger(this);
			mMessenger.send(request);
		} catch (RemoteException e) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "Request CANCEL", e);
			}
		}
	}

	@Override
	public void handleMessage(Message msg) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "handleMessage");

		switch (msg.what) {
		case PsService.MSG_RESPONSE_INIT_LIBRARY:
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "handleMessage : case PsService.MSG_RESPONSE_INIT_LIBRARY");
			}
			mListener.Ps_Sample_Apl_Java_NotifyResult_InitLibrary(
					PsServiceHelper.getBundleToPsThreadResult(msg.getData()),
					PsServiceHelper.getBundleToSensorType(msg.getData()),
					PsServiceHelper.getBundleToSensorExtKind(msg.getData()));
			break;
		case PsService.MSG_RESPONSE_TERM_LIBRARY:
			break;
		case PsService.MSG_RESPONSE_ENROLL:
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "handleMessage : case PsService.MSG_RESPONSE_ENROLL");
			}
			mListener.Ps_Sample_Apl_Java_NotifyResult_Enroll(
					PsServiceHelper.getBundleToPsThreadResult(msg.getData()),
					PsServiceHelper.getBundleToEnrollScore(msg.getData()));
			break;
		case PsService.MSG_RESPONSE_VERIFY:
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "handleMessage : case PsService.MSG_RESPONSE_VERIFY");
			}
			mListener.Ps_Sample_Apl_Java_NotifyResult_Verify(
					PsServiceHelper.getBundleToPsThreadResult(msg.getData()));
			break;
		case PsService.MSG_RESPONSE_IDENTIFY:
			if (BuildConfig.DEBUG) {
				Log.d(TAG,
						"handleMessage : case PsService.MSG_RESPONSE_IDENTIFY");
			}
			mListener.Ps_Sample_Apl_Java_NotifyResult_Identify(
					PsServiceHelper.getBundleToPsThreadResult(msg.getData()));
			break;
		case PsService.MSG_RESPONSE_CANCEL:
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "handleMessage : case PsService.MSG_RESPONSE_CANCEL");
			}
			mListener.Ps_Sample_Apl_Java_NotifyResult_Cancel(
					PsServiceHelper.getBundleToPsThreadResult(msg.getData()));
			break;
		case PsService.MSG_RESPONSE_MESSAGE:
			if (BuildConfig.DEBUG) {
				Log.d(TAG,
						"handleMessage : case PsService.MSG_RESPONSE_MESSAGE");
			}
			mListener.Ps_Sample_Apl_Java_NotifyWorkMessage(
					PsServiceHelper.getBundleToProcessKey(msg.getData()));
			break;
		case PsService.MSG_RESPONSE_MESSAGE_COUNT:
			if (BuildConfig.DEBUG) {
				Log.d(TAG,
						"handleMessage : case PsService.MSG_RESPONSE_MESSAGE_COUNT");
			}
			mListener.Ps_Sample_Apl_Java_NotifyWorkMessage(
					PsServiceHelper.getBundleToProcessKey(msg.getData()),
					PsServiceHelper.getBundleToCount(msg.getData()),
					PsServiceHelper.getBundleToCapNumber(msg.getData()));
			break;
		case PsService.MSG_RESPONSE_GUIDANCE:
			if (BuildConfig.DEBUG) {
				Log.d(TAG,
						"handleMessage : case PsService.MSG_RESPONSE_GUIDANCE");
			}
			mListener.Ps_Sample_Apl_Java_NotifyGuidance(
					PsServiceHelper.getBundleToGuidanceKey(msg.getData()),
					PsServiceHelper.getBundleToGuidanceError(msg.getData()));
			break;
		case PsService.MSG_RESPONSE_SILHOUETTE:
			if (BuildConfig.DEBUG) {
				Log.d(TAG,
						"handleMessage : case PsService.MSG_RESPONSE_SILHOUETTE");
			}
			mListener.Ps_Sample_Apl_Java_NotifySilhouette(
					PsServiceHelper.getBundleToSilhouette(msg.getData()));
			break;
		default:
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "handleMessage : default");
			}
			super.handleMessage(msg);
			break;
		}
	}
}
