/*
 * PsStateCallback.java
 *
 *	All Rights Reserved, Copyright(c) FUJITSU FRONTECH LIMITED 2021
 */

package com.fujitsu.frontech.palmsecure_sample.service;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.fujitsu.frontech.palmsecure.JAVA_BioAPI_GUI_BITMAP;
import com.fujitsu.frontech.palmsecure.JAVA_BioAPI_GUI_STATE_CALLBACK_IF;
import com.fujitsu.frontech.palmsecure.util.PalmSecureConstant;
import com.fujitsu.frontech.palmsecure_sample.BuildConfig;
import com.fujitsu.frontech.palmsecure_sample.R;
import com.fujitsu.frontech.palmsecure_sample.data.PsLogManager;

public class PsStateCallback implements JAVA_BioAPI_GUI_STATE_CALLBACK_IF {

	private static final String TAG = "PsStateCallback";
	private long mMessage = 0;

	@Override
	public long JAVA_BioAPI_GUI_STATE_CALLBACK(
			Object GuiStateCallbackCtx,
			long GuiState,
			short Response,
			long Message,
			short Progress,
			JAVA_BioAPI_GUI_BITMAP SampleBuffer) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "JAVA_BioAPI_GUI_STATE_CALLBACK");
		}

		PsService service = (PsService) GuiStateCallbackCtx;

		if ((GuiState & PalmSecureConstant.JAVA_BioAPI_SAMPLE_AVAILABLE) == PalmSecureConstant.JAVA_BioAPI_SAMPLE_AVAILABLE) {

			if (service.mResponseMessenger != null) {
				try {
					Message response = android.os.Message.obtain(
							null,
							PsService.MSG_RESPONSE_SILHOUETTE);
					Bundle b = new Bundle();
					PsServiceHelper.putSilhouetteToBundle(b, SampleBuffer);
					response.setData(b);
					service.mResponseMessenger.send(response);
					service.silhouette = SampleBuffer.Bitmap.Data;
				} catch (RemoteException e) {
					if (BuildConfig.DEBUG) {
						Log.e(TAG, "MSG_RESPONSE_SILHOUETTE", e);
					}
				}
			}
		}

		if ((GuiState & PalmSecureConstant.JAVA_BioAPI_MESSAGE_PROVIDED) == PalmSecureConstant.JAVA_BioAPI_MESSAGE_PROVIDED) {

			//Get template quality
			if ((Message & 0xff000000) == PalmSecureConstant.JAVA_PvAPI_NOTIFY_REGIST_SCORE) {
				service.notifiedScore = (int) (Message & 0x0000000f);
				return PalmSecureConstant.JAVA_BioAPI_OK;
			}

			//Get number of capture
			if ((Message & 0xffffff00) == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_START) {

				if (service.enrollFlg == true) {
					if (service.mResponseMessenger != null) {
						try {
							Message response = android.os.Message.obtain(
									null,
									PsService.MSG_RESPONSE_MESSAGE_COUNT);
							Bundle b = new Bundle();
							PsServiceHelper.putProcessKeyToBundle(b, R.string.WorkEnroll);
							PsServiceHelper.putCountToBundle(b, (int) (Message & 0x0000000f));
							PsServiceHelper.putCapNumberToBundle(b, (int) (Message & 0x000000f0) >> 4);
							response.setData(b);
							service.mResponseMessenger.send(response);
						} catch (RemoteException e) {
							if (BuildConfig.DEBUG) {
								Log.e(TAG, "MSG_RESPONSE_MESSAGE_COUNT", e);
							}
						}
					}
				}
				mMessage = 0;
				return PalmSecureConstant.JAVA_BioAPI_OK;
			}

			if (Message == mMessage)
				return PalmSecureConstant.JAVA_BioAPI_OK;
			mMessage = Message;

			int key = 0;
			if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_MOVING) {
				key = R.string.NOTIFY_CAP_GUID_MOVING;

			} else if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_NO_HANDS) {
				key = R.string.NOTIFY_CAP_GUID_NO_HANDS;

			} else if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_LESSINFO) {
				key = R.string.NOTIFY_CAP_GUID_LESSINFO;

			} else if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_FAR) {
				key = R.string.NOTIFY_CAP_GUID_FAR;

			} else if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_NEAR) {
				key = R.string.NOTIFY_CAP_GUID_NEAR;

			} else if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_CAPTURING) {
				key = R.string.NOTIFY_CAP_GUID_CAPTURING;

			} else if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_PHASE_END) {
				key = R.string.NOTIFY_CAP_GUID_PHASE_END;

			} else if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_RIGHT) {
				key = R.string.NOTIFY_CAP_GUID_RIGHT;

			} else if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_LEFT) {
				key = R.string.NOTIFY_CAP_GUID_LEFT;

			} else if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_DOWN) {
				key = R.string.NOTIFY_CAP_GUID_DOWN;

			} else if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_UP) {
				key = R.string.NOTIFY_CAP_GUID_START;

			} else if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_PITCH_DOWN) {
				key = R.string.NOTIFY_CAP_GUID_PITCH_DOWN;

			} else if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_PITCH_UP) {
				key = R.string.NOTIFY_CAP_GUID_PITCH_UP;

			} else if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_ROLL_RIGHT) {
				key = R.string.NOTIFY_CAP_GUID_ROLL_RIGHT;

			} else if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_ROLL_LEFT) {
				key = R.string.NOTIFY_CAP_GUID_ROLL_LEFT;

			} else if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_YAW_RIGHT) {
				key = R.string.NOTIFY_CAP_GUID_YAW_RIGHT;

			} else if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_YAW_LEFT) {
				key = R.string.NOTIFY_CAP_GUID_YAW_LEFT;

			} else if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_ROUND) {
				key = R.string.NOTIFY_CAP_GUID_ROUND;

			} else if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_ADJUST_LIGHT) {
				key = R.string.NOTIFY_CAP_GUID_ADJUST_LIGHT;

			} else if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_ADJUST_NG) {
				key = R.string.NOTIFY_CAP_GUID_ADJUST_NG;

			} else if (Message == PalmSecureConstant.JAVA_PvAPI_NOTIFY_CAP_GUID_BADIMAGE) {
				key = R.string.NOTIFY_CAP_GUID_BADIMAGE;

			} else {
				return PalmSecureConstant.JAVA_BioAPI_OK;
			}

			if (service.mResponseMessenger != null) {
				try {
					Message response = android.os.Message.obtain(
							null,
							PsService.MSG_RESPONSE_GUIDANCE);
					Bundle b = new Bundle();
					PsServiceHelper.putGuidanceKeyToBundle(b, key);
					PsServiceHelper.putGuidanceErrorToBundle(b, false);
					response.setData(b);
					service.mResponseMessenger.send(response);
				} catch (RemoteException e) {
					if (BuildConfig.DEBUG) {
						Log.e(TAG, "MSG_RESPONSE_GUIDANCE", e);
					}
				}
			}
		}

		return PalmSecureConstant.JAVA_BioAPI_OK;

	}
}
