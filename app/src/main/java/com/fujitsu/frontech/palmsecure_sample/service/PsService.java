/*
 * PsService.java
 *
 *	All Rights Reserved, Copyright(c) FUJITSU FRONTECH LIMITED 2021
 */

package com.fujitsu.frontech.palmsecure_sample.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.fujitsu.frontech.palmsecure.JAVA_PvAPI_LBINFO;
import com.fujitsu.frontech.palmsecure.JAVA_uint32;
import com.fujitsu.frontech.palmsecure.PalmSecureIf;
import com.fujitsu.frontech.palmsecure.util.PalmSecureConstant;
import com.fujitsu.frontech.palmsecure.util.PalmSecureException;
import com.fujitsu.frontech.palmsecure_sample.BuildConfig;
import com.fujitsu.frontech.palmsecure_sample.R;
import com.fujitsu.frontech.palmsecure_sample.data.PsThreadResult;

import java.io.IOException;

public class PsService extends Service {

	private static final String TAG = "PsService";

	public static final int MSG_REQUEST_INIT_LIBRARY = 10;
	public static final int MSG_REQUEST_ATTACH = 20;
	public static final int MSG_REQUEST_TERM_LIBRARY = 30;
	public static final int MSG_REQUEST_ENROLL = 40;
	public static final int MSG_REQUEST_VERIFY = 50;
	public static final int MSG_REQUEST_IDENTIFY = 60;
	public static final int MSG_REQUEST_CANCEL = 70;

	public static final int MSG_RESPONSE_INIT_LIBRARY = 1010;
	public static final int MSG_RESPONSE_ATTACH = 1020;
	public static final int MSG_RESPONSE_TERM_LIBRARY = 1030;
	public static final int MSG_RESPONSE_ENROLL = 1040;
	public static final int MSG_RESPONSE_VERIFY = 1050;
	public static final int MSG_RESPONSE_IDENTIFY = 1060;
	public static final int MSG_RESPONSE_CANCEL = 1070;

	public static final int MSG_RESPONSE_MESSAGE = 2010;
	public static final int MSG_RESPONSE_MESSAGE_COUNT = 2011;
	public static final int MSG_RESPONSE_GUIDANCE = 2020;
	public static final int MSG_RESPONSE_SILHOUETTE = 2030;

	public Messenger mResponseMessenger = null;
	public boolean cancelFlg = false;
	public boolean enrollFlg = false;
	public int notifiedScore = 0;
	public int mUsingDataType = 0;
	public int mUsingGuideMode = 0;
	public long mUsingSensorType = 0;
	public long mUsingSensorTypeReal = 0;
	private long mUsingSensorExtKind = 0;

	public byte[] silhouette = null;

	private PsStateCallback psStateCB = null;
	private PsStreamingCallback psStreamingCB = null;
	private PalmSecureIf palmsecureIf = null;
	private JAVA_uint32 moduleHandle = null;

	private final Messenger mMessenger = new Messenger(new ServiceHandler());

	private static final byte[] ModuleGuid = new byte[] {
			(byte) 0xe1, (byte) 0x9a, (byte) 0x69, (byte) 0x01,
			(byte) 0xb8, (byte) 0xc2, (byte) 0x49, (byte) 0x80,
			(byte) 0x87, (byte) 0x7e, (byte) 0x11, (byte) 0xd4,
			(byte) 0xd8, (byte) 0xf1, (byte) 0xbe, (byte) 0x79
	};

	@Override
	public void onCreate() {
		super.onCreate();
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onCreate");
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onStartCommand Received start id " + startId + ": " + intent);
		}
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onBind");
		}
		return mMessenger.getBinder();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onDestroy");
		}
	}

	private class ServiceHandler extends Handler {
		@Override
		public void handleMessage(Message request) {

			switch (request.what) {
			case MSG_REQUEST_INIT_LIBRARY:
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "handleMessage : case MSG_REQUEST_INIT_LIBRARY");
				}

				if (request.replyTo != null) {
					mResponseMessenger = request.replyTo;

					// Initialize the Authentication library.
					PsThreadResult stResult = Ps_Sample_Apl_Java_InitLibrary(
							PsServiceHelper.getBundleToApplicationKey(request.getData()),
							PsServiceHelper.getBundleToGuideMode(request.getData()),
							PsServiceHelper.getBundleToDataType(request.getData()));

					try {
						Message response = Message.obtain(null, PsService.MSG_RESPONSE_INIT_LIBRARY);
						Bundle b = new Bundle();
						PsServiceHelper.putPsThreadResultToBundle(b, stResult);
						PsServiceHelper.putSensorTypeToBundle(b, mUsingSensorTypeReal);
						PsServiceHelper.putSensorExtKindToBundle(b, mUsingSensorExtKind);
						response.setData(b);
						mResponseMessenger.send(response);
					} catch (RemoteException e) {
						if (BuildConfig.DEBUG) {
							Log.e(TAG, "NotifyResult_InitLibrary", e);
						}
					}
				}
				else {
					if (BuildConfig.DEBUG) {
						Log.e(TAG, "MSG_REQUEST_INIT_LIBRARY : replyTo is null.");
					}
				}
				break;
			case MSG_REQUEST_TERM_LIBRARY:
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "handleMessage : case MSG_REQUEST_TERM_LIBRARY");
				}

				if (request.replyTo != null && palmsecureIf != null) {
					mResponseMessenger = request.replyTo;

					// Terminate the Authentication library.
					Ps_Sample_Apl_Java_TermLibrary();
				}
				else {
					if (BuildConfig.DEBUG) {
						Log.e(TAG, "MSG_REQUEST_TERM_LIBRARY : replyTo or palmsecureIf is null.");
					}
				}
				break;
			case MSG_REQUEST_ENROLL:
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "handleMessage : case MSG_REQUEST_ENROLL");
				}

				if (request.replyTo != null) {
					mResponseMessenger = request.replyTo;

					PsThreadEnroll threadEnroll = new PsThreadEnroll(
							PsService.this,
							palmsecureIf,
							moduleHandle,
							PsServiceHelper.getBundleToUserId(request.getData()),
							PsServiceHelper.getBundleToNumberOfRetry(request.getData()),
							PsServiceHelper.getBundleToSleepTime(request.getData()));
					threadEnroll.start();
				}
				else {
					if (BuildConfig.DEBUG) {
						Log.e(TAG, "MSG_REQUEST_ENROLL : replyTo is null.");
					}
				}
				break;
			case MSG_REQUEST_VERIFY:
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "handleMessage : case MSG_REQUEST_VERIFY.");
				}

				if (request.replyTo != null) {
					mResponseMessenger = request.replyTo;

					PsThreadVerify verifyThread = new PsThreadVerify(
							PsService.this,
							palmsecureIf,
							moduleHandle,
							PsServiceHelper.getBundleToUserId(request.getData()),
							PsServiceHelper.getBundleToNumberOfRetry(request.getData()),
							PsServiceHelper.getBundleToSleepTime(request.getData()));
					verifyThread.start();
				}
				else {
					if (BuildConfig.DEBUG) {
						Log.e(TAG, "MSG_REQUEST_VERIFY : replyTo is null.");
					}
				}
				break;
			case MSG_REQUEST_IDENTIFY:
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "handleMessage : case MSG_REQUEST_IDENTIFY.");
				}

				if (request.replyTo != null) {
					mResponseMessenger = request.replyTo;

					PsThreadIdentify identifyThread = new PsThreadIdentify(
							PsService.this,
							palmsecureIf,
							moduleHandle,
							PsServiceHelper.getBundleToNumberOfRetry(request.getData()),
							PsServiceHelper.getBundleToSleepTime(request.getData()),
							PsServiceHelper.getBundleToMaxResults(request.getData()));
					identifyThread.start();
				}
				else {
					if (BuildConfig.DEBUG) {
						Log.e(TAG, "MSG_REQUEST_IDENTIFY : replyTo is null.");
					}
				}
				break;
			case MSG_REQUEST_CANCEL:
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "handleMessage : case MSG_REQUEST_CANCEL.");
				}

				if (request.replyTo != null) {
					mResponseMessenger = request.replyTo;

					PsThreadCancel cancelThread = new PsThreadCancel(
							PsService.this,
							palmsecureIf,
							moduleHandle);
					cancelThread.start();
					cancelFlg = true;
				}
				else {
					if (BuildConfig.DEBUG) {
						Log.e(TAG, "MSG_REQUEST_CANCEL : replyTo is null.");
					}
				}
				break;
			default:
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "handleMessage : default.");
				}

				super.handleMessage(request);
				break;
			}
		}
	}

	/**
	 * Initialize the Authentication library.
	 */
	private PsThreadResult Ps_Sample_Apl_Java_InitLibrary(String aplKey, int guideMode, int dataType) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "Ps_Sample_Apl_Java_InitLibrary: aplKey=" + aplKey
				+ " guideMode=" + guideMode + " dataType=" + dataType);
		}

		mUsingDataType = dataType;

		PsThreadResult stResult = new PsThreadResult();
		JAVA_PvAPI_LBINFO lbInfo = new JAVA_PvAPI_LBINFO();

		//Create a instance of PalmSecureIf class
		///////////////////////////////////////////////////////////////////////////
		try {
			palmsecureIf = new PalmSecureIf(this);
		} catch (PalmSecureException e) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "Create a instance of PalmSecureIf class", e);
			}
			stResult.result = PalmSecureConstant.JAVA_BioAPI_ERRCODE_FUNCTION_FAILED;
			stResult.pseErrNumber = e.ErrNumber;
			return stResult;
		}
		///////////////////////////////////////////////////////////////////////////

		//Authenticate application by key
		///////////////////////////////////////////////////////////////////////////
		try {
			stResult.result = palmsecureIf.JAVA_PvAPI_ApAuthenticate(aplKey);
			if (stResult.result != PalmSecureConstant.JAVA_BioAPI_OK) {
				palmsecureIf.JAVA_PvAPI_GetErrorInfo(stResult.errInfo);
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "Authenticate application by key, PalmSecure method failed");
				}
				return stResult;
			}
		} catch (PalmSecureException e) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "Authenticate application by key", e);
			}
			stResult.result = PalmSecureConstant.JAVA_BioAPI_ERRCODE_FUNCTION_FAILED;
			stResult.pseErrNumber = e.ErrNumber;
			return stResult;
		}
		if (BuildConfig.DEBUG)
			Log.d(TAG, "PvAPI_ApAuthenticate Done!");
		///////////////////////////////////////////////////////////////////////////

		//Load module
		///////////////////////////////////////////////////////////////////////////
		try {
			stResult.result = palmsecureIf.JAVA_BioAPI_ModuleLoad(ModuleGuid, null, null, null);
			if (stResult.result != PalmSecureConstant.JAVA_BioAPI_OK) {
				palmsecureIf.JAVA_PvAPI_GetErrorInfo(stResult.errInfo);
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "Load module, PalmSecure method failed");
				}
				return stResult;
			}
		} catch (PalmSecureException e) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "Load module", e);
			}
			stResult.result = PalmSecureConstant.JAVA_BioAPI_ERRCODE_FUNCTION_FAILED;
			stResult.pseErrNumber = e.ErrNumber;
			return stResult;
		}
		if (BuildConfig.DEBUG)
			Log.d(TAG, "BioAPI_ModuleLoad Done!");
		///////////////////////////////////////////////////////////////////////////

		//Set GExtended mode
		///////////////////////////////////////////////////////////////////////////
		JAVA_uint32 uiFlag = new JAVA_uint32();
		uiFlag.value = PalmSecureConstant.JAVA_PvAPI_PRE_PROFILE_G_EXTENDED_MODE;
		JAVA_uint32 lpvParamData = new JAVA_uint32();
		if ( mUsingDataType == 0 || mUsingDataType == 1 ) {
			lpvParamData.value = (int)PalmSecureConstant.JAVA_PvAPI_PRE_PROFILE_G_EXTENDED_MODE_OFF;
		} else if ( mUsingDataType == 2 ){
			lpvParamData.value = (int)PalmSecureConstant.JAVA_PvAPI_PRE_PROFILE_G_EXTENDED_MODE_1;
		} else {
			lpvParamData.value = (int)PalmSecureConstant.JAVA_PvAPI_PRE_PROFILE_G_EXTENDED_MODE_2;
		}
		
		try {
			stResult.result = palmsecureIf.JAVA_PvAPI_PreSetProfile(
					uiFlag,
					lpvParamData,
					null,
					null);
			if (stResult.result != PalmSecureConstant.JAVA_BioAPI_OK) {
				palmsecureIf.JAVA_PvAPI_GetErrorInfo(stResult.errInfo);
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "PreSetProfile, PalmSecure method failed");
				}
				return stResult;
			}
		} catch(PalmSecureException e) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "PreSetProfile", e);
			}
			stResult.result = PalmSecureConstant.JAVA_BioAPI_ERRCODE_FUNCTION_FAILED;
 			stResult.pseErrNumber = e.ErrNumber;
			return stResult;
		}
		if (BuildConfig.DEBUG)
			Log.d(TAG, "BioAPI_PreSetProfile Done!");
		///////////////////////////////////////////////////////////////////////////

		//Attatch to module
		///////////////////////////////////////////////////////////////////////////
		try {
			moduleHandle = new JAVA_uint32();
			stResult.result = palmsecureIf.JAVA_BioAPI_ModuleAttach(
					ModuleGuid,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					moduleHandle);
			if (stResult.result != PalmSecureConstant.JAVA_BioAPI_OK) {
				palmsecureIf.JAVA_PvAPI_GetErrorInfo(stResult.errInfo);
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "Attatch to module, PalmSecure method failed");
				}
				return stResult;
			}
		} catch (PalmSecureException e) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "Attatch to module", e);
			}
			stResult.result = PalmSecureConstant.JAVA_BioAPI_ERRCODE_FUNCTION_FAILED;
			stResult.pseErrNumber = e.ErrNumber;
			return stResult;
		}
		if (BuildConfig.DEBUG)
			Log.d(TAG, "BioAPI_ModuleAttach Done!");
		///////////////////////////////////////////////////////////////////////////

		//Set action listener
		///////////////////////////////////////////////////////////////////////////
		try {
			psStreamingCB = new PsStreamingCallback();
			psStateCB = new PsStateCallback();
			stResult.result = palmsecureIf.JAVA_BioAPI_SetGUICallbacks(
					moduleHandle,
					psStreamingCB,
					this,
					psStateCB,
					this);
			if (stResult.result != PalmSecureConstant.JAVA_BioAPI_OK) {
				palmsecureIf.JAVA_PvAPI_GetErrorInfo(stResult.errInfo);
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "Set action listener, PalmSecure method failed");
				}
				return stResult;
			}
		} catch (PalmSecureException e) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "Set action listener", e);
			}
			stResult.result = PalmSecureConstant.JAVA_BioAPI_ERRCODE_FUNCTION_FAILED;
			stResult.pseErrNumber = e.ErrNumber;
			return stResult;
		}
		if (BuildConfig.DEBUG)
			Log.d(TAG, "BioAPI_SetGUICallbacks Done!");
		///////////////////////////////////////////////////////////////////////////

		//Get library information
		///////////////////////////////////////////////////////////////////////////
		try {
			stResult.result = palmsecureIf.JAVA_PvAPI_GetLibraryInfo(lbInfo);
			if (stResult.result != PalmSecureConstant.JAVA_BioAPI_OK) {
				palmsecureIf.JAVA_PvAPI_GetErrorInfo(stResult.errInfo);
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "Get library information, PalmSecure method failed");
				}
				return stResult;
			}
		} catch (PalmSecureException e) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "Get library information", e);
			}
			stResult.result = PalmSecureConstant.JAVA_BioAPI_ERRCODE_FUNCTION_FAILED;
			stResult.pseErrNumber = e.ErrNumber;
			return stResult;
		}
		if (BuildConfig.DEBUG)
			Log.d(TAG, "PvAPI_GetLibraryInfo Done!");
		///////////////////////////////////////////////////////////////////////////

		mUsingSensorType = (int) lbInfo.uiSensorKind;
		mUsingSensorTypeReal = mUsingSensorType;
		mUsingSensorExtKind = lbInfo.uiSensorExtKind;
		switch ((int )mUsingSensorType) {
			case (int) PalmSecureConstant.JAVA_PvAPI_INFO_SENSOR_TYPE_2:
				break;
			case (int) PalmSecureConstant.JAVA_PvAPI_INFO_SENSOR_TYPE_9:
				mUsingSensorType = PalmSecureConstant.JAVA_PvAPI_INFO_SENSOR_TYPE_2;
				break;
			case (int) PalmSecureConstant.JAVA_PvAPI_INFO_SENSOR_TYPE_B:
				break;
			case (int) PalmSecureConstant.JAVA_PvAPI_INFO_SENSOR_TYPE_D:
				break;
		}

		mUsingGuideMode = guideMode;

		//Set guide mode
		///////////////////////////////////////////////////////////////////////////
		try {
			JAVA_uint32 dwFlag = new JAVA_uint32();
			dwFlag.value = PalmSecureConstant.JAVA_PvAPI_PROFILE_GUIDE_MODE;
			JAVA_uint32 dwParam1 = new JAVA_uint32();
			if (mUsingGuideMode == 0) {
				dwParam1.value = (int) PalmSecureConstant.JAVA_PvAPI_PROFILE_GUIDE_MODE_NO_GUIDE;
			} else {
				dwParam1.value = (int) PalmSecureConstant.JAVA_PvAPI_PROFILE_GUIDE_MODE_GUIDE;
			}
			stResult.result = palmsecureIf.JAVA_PvAPI_SetProfile(
					moduleHandle,
					dwFlag,
					dwParam1,
					null,
					null);
			if (stResult.result != PalmSecureConstant.JAVA_BioAPI_OK) {
				palmsecureIf.JAVA_PvAPI_GetErrorInfo(stResult.errInfo);
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "Set guide mode, PalmSecure method failed");
				}
				return stResult;
			}
		} catch (PalmSecureException e) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "Set guide mode", e);
			}
			stResult.result = PalmSecureConstant.JAVA_BioAPI_ERRCODE_FUNCTION_FAILED;
			stResult.pseErrNumber = e.ErrNumber;
			return stResult;
		}
		if (BuildConfig.DEBUG)
			Log.d(TAG, "PvAPI_SetProfile(set guide mode) Done!");
		///////////////////////////////////////////////////////////////////////////



		return stResult;
	}

	/**
	 * Terminate the Authentication library.
	 */
	private void Ps_Sample_Apl_Java_TermLibrary() {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "Ps_Sample_Apl_Java_TermLibrary");
		}

		//Detach module
		///////////////////////////////////////////////////////////////////////////
		if (moduleHandle != null) {
			try {
				palmsecureIf.JAVA_BioAPI_ModuleDetach(
						moduleHandle);
			} catch (PalmSecureException e) {
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "Detach module", e);
				}
			}
			if (BuildConfig.DEBUG)
				Log.d(TAG, "BioAPI_ModuleDetach Done!");
		}
		///////////////////////////////////////////////////////////////////////////

		//Unload module
		///////////////////////////////////////////////////////////////////////////
		try {
			palmsecureIf.JAVA_BioAPI_ModuleUnload(
					ModuleGuid,
					null,
					null);
		} catch (PalmSecureException e) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "Unload module", e);
			}
		}
		if (BuildConfig.DEBUG)
			Log.d(TAG, "BioAPI_ModuleUnload Done!");
		///////////////////////////////////////////////////////////////////////////

		return;

	}
}
