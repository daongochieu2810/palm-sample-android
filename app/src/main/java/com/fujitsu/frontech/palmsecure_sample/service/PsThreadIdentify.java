/*
 * PsThreadIdentify.java
 *
 *	All Rights Reserved, Copyright(c) FUJITSU FRONTECH LIMITED 2021
 */

package com.fujitsu.frontech.palmsecure_sample.service;

import java.util.ArrayList;

import android.util.Log;

import com.fujitsu.frontech.palmsecure.JAVA_BioAPI_CANDIDATE;
import com.fujitsu.frontech.palmsecure.JAVA_BioAPI_IDENTIFY_POPULATION;
import com.fujitsu.frontech.palmsecure.JAVA_sint32;
import com.fujitsu.frontech.palmsecure.JAVA_uint32;
import com.fujitsu.frontech.palmsecure.PalmSecureIf;
import com.fujitsu.frontech.palmsecure.util.PalmSecureConstant;
import com.fujitsu.frontech.palmsecure.util.PalmSecureException;
import com.fujitsu.frontech.palmsecure_sample.BuildConfig;
import com.fujitsu.frontech.palmsecure_sample.R;
import com.fujitsu.frontech.palmsecure_sample.data.PsDataManager;
import com.fujitsu.frontech.palmsecure_sample.data.PsLogManager;
import com.fujitsu.frontech.palmsecure_sample.data.PsThreadResult;
import com.fujitsu.frontech.palmsecure_sample.exception.PsAplException;

public class PsThreadIdentify extends PsThreadBase {

	public PsThreadIdentify(PsService service, PalmSecureIf palmsecureIf, JAVA_uint32 moduleHandle, int numberOfRetry,
			int sleepTime, long maxResults) {

		super("PsThreadIdentify", service, palmsecureIf, moduleHandle, "", numberOfRetry, sleepTime, maxResults);
	}

	public void run() {

		PsThreadResult stResult = null;
		PsDataManager dataMng = new PsDataManager(
				this.service.getBaseContext(),
				this.service.mUsingSensorType,
				this.service.mUsingDataType);

		try {
			int waitTime = 0;

			//Create a instance of DNET_BioAPI_IDENTIFY_POPULATION class
			///////////////////////////////////////////////////////////////////////////
			ArrayList<String> idList = new ArrayList<String>();
			JAVA_BioAPI_IDENTIFY_POPULATION Population = null;
			try {
				Population = dataMng.convertDBToBioAPI_Data_All(idList);
			} catch (PalmSecureException e) {
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "Create a instance of DNET_BioAPI_IDENTIFY_POPULATION class", e);
				}
				stResult = new PsThreadResult();
				stResult.result = PalmSecureConstant.JAVA_BioAPI_ERRCODE_FUNCTION_FAILED;
				stResult.pseErrNumber = e.ErrNumber;
				Ps_Sample_Apl_Java_NotifyResult_Identify(stResult);
				return;
			} catch (PsAplException pae) {
				if (BuildConfig.DEBUG) {
					Log.e(TAG, "Create a instance of DNET_BioAPI_IDENTIFY_POPULATION class", pae);
				}
				stResult = new PsThreadResult();
				stResult.result = PalmSecureConstant.JAVA_BioAPI_ERRCODE_FUNCTION_FAILED;
				stResult.messageKey = R.string.AplErrorSystemError;
				Ps_Sample_Apl_Java_NotifyResult_Identify(stResult);
				return;
			}
			///////////////////////////////////////////////////////////////////////////

			//Repeat numOfRetry times until identification succeed
			for (int identifyCnt = 0; identifyCnt <= this.numberOfRetry; identifyCnt++) {

				Ps_Sample_Apl_Java_NotifyWorkMessage(R.string.WorkIdentify);

				if (identifyCnt > 0) {

					Ps_Sample_Apl_Java_NotifyGuidance(
							R.string.RetryTransaction,
							false);

					waitTime = 0;
					do {
						//End transaction in case of cancel
						if (this.service.cancelFlg == true) {
							break;
						}

						if (waitTime < this.sleepTime) {
							Thread.sleep(100);
							waitTime = waitTime + 100;
						} else {
							break;
						}
					} while (true);
				}

				//End transaction in case of cancel
				if (this.service.cancelFlg == true) {
					break;
				}

				stResult = new PsThreadResult();

				//Set mode to get authentication score
				///////////////////////////////////////////////////////////////////////////
				try {
					JAVA_uint32 dwFlag = new JAVA_uint32();
					dwFlag.value = PalmSecureConstant.JAVA_PvAPI_PROFILE_SCORE_NOTIFICATIONS;
					JAVA_uint32 dwParam1 = new JAVA_uint32();
					dwParam1.value = PalmSecureConstant.JAVA_PvAPI_PROFILE_SCORE_NOTIFICATIONS_ON;
					stResult.result = palmsecureIf.JAVA_PvAPI_SetProfile(
							moduleHandle,
							dwFlag,
							dwParam1,
							null,
							null);
				} catch (PalmSecureException e) {
					if (BuildConfig.DEBUG) {
						Log.e(TAG, "Set mode to get authentication score", e);
					}
					stResult.result = PalmSecureConstant.JAVA_BioAPI_ERRCODE_FUNCTION_FAILED;
					stResult.pseErrNumber = e.ErrNumber;
					break;
				}
				///////////////////////////////////////////////////////////////////////////

				//If PalmSecure method failed, get error info
				if (stResult.result != PalmSecureConstant.JAVA_BioAPI_OK) {
					try {
						palmsecureIf.JAVA_PvAPI_GetErrorInfo(stResult.errInfo);
					} catch (PalmSecureException e) {
						if (BuildConfig.DEBUG) {
							Log.e(TAG, "Set mode to get authentication score, get error info", e);
						}
						stResult.pseErrNumber = e.ErrNumber;
					}
					break;
				}

				stResult.retryCnt = identifyCnt;

				//Identification
				///////////////////////////////////////////////////////////////////////////
				JAVA_sint32 maxFRRRequested = new JAVA_sint32();
				maxFRRRequested.value = PalmSecureConstant.JAVA_PvAPI_MATCHING_LEVEL_NORMAL;
				JAVA_uint32 farPrecedence = new JAVA_uint32();
				farPrecedence.value = PalmSecureConstant.JAVA_BioAPI_FALSE;
				JAVA_uint32 binning = new JAVA_uint32();
				binning.value = PalmSecureConstant.JAVA_BioAPI_FALSE;
				JAVA_uint32 maxNumberOfResults = new JAVA_uint32();
				maxNumberOfResults.value = this.maxResults;
				JAVA_uint32 numberOfResults = new JAVA_uint32();
				JAVA_BioAPI_CANDIDATE[] candidates = new JAVA_BioAPI_CANDIDATE[(int) maxNumberOfResults.value];
				JAVA_sint32 timeout = new JAVA_sint32();
				timeout.value = 0;
				try {
					stResult.result = palmsecureIf.JAVA_BioAPI_Identify(
							moduleHandle,
							null,
							maxFRRRequested,
							farPrecedence,
							Population,
							binning,
							maxNumberOfResults,
							numberOfResults,
							candidates,
							timeout,
							null);
				} catch (PalmSecureException e) {
					if (BuildConfig.DEBUG) {
						Log.e(TAG, "Identification", e);
					}
					stResult.result = PalmSecureConstant.JAVA_BioAPI_ERRCODE_FUNCTION_FAILED;
					stResult.pseErrNumber = e.ErrNumber;
					break;
				}
				///////////////////////////////////////////////////////////////////////////

				//If PalmSecure method failed, get error info
				if ((stResult.result != PalmSecureConstant.JAVA_BioAPI_OK)
						&& (this.service.cancelFlg != true)) {
					try {
						palmsecureIf.JAVA_PvAPI_GetErrorInfo(stResult.errInfo);
					} catch (PalmSecureException e) {
						if (BuildConfig.DEBUG) {
							Log.e(TAG, "Identification, get error info", e);
						}
						stResult.pseErrNumber = e.ErrNumber;
					}
					break;
				}

				//Set mode not to get authentication score
				///////////////////////////////////////////////////////////////////////////
				try {
					JAVA_uint32 dwFlag = new JAVA_uint32();
					dwFlag.value = PalmSecureConstant.JAVA_PvAPI_PROFILE_SCORE_NOTIFICATIONS;
					JAVA_uint32 dwParam1 = new JAVA_uint32();
					dwParam1.value = PalmSecureConstant.JAVA_PvAPI_PROFILE_SCORE_NOTIFICATIONS_OFF;
					stResult.result = palmsecureIf.JAVA_PvAPI_SetProfile(
							moduleHandle,
							dwFlag,
							dwParam1,
							null,
							null);

				} catch (PalmSecureException e) {
					if (BuildConfig.DEBUG) {
						Log.e(TAG, "Set mode not to get authentication score", e);
					}
					stResult.result = PalmSecureConstant.JAVA_BioAPI_ERRCODE_FUNCTION_FAILED;
					stResult.pseErrNumber = e.ErrNumber;
					break;
				}
				///////////////////////////////////////////////////////////////////////////

				//End transaction in case of cancel
				if (this.service.cancelFlg == true) {
					break;
				}

				//If PalmSecure method failed, get error info
				if (stResult.result != PalmSecureConstant.JAVA_BioAPI_OK) {
					try {
						palmsecureIf.JAVA_PvAPI_GetErrorInfo(stResult.errInfo);
					} catch (PalmSecureException e) {
						if (BuildConfig.DEBUG) {
							Log.e(TAG, "Set mode not to get authentication score, get error info", e);
						}
						stResult.pseErrNumber = e.ErrNumber;
					}
					break;
				}

				stResult.info = this.service.silhouette;

				//If result of identification is 0, retry identification
				if (numberOfResults.value == 0) {
					continue;
				}
				if (numberOfResults.value >= 1) {
					for (int i = 0; i < numberOfResults.value; i++) {
						stResult.farAchieved.add((int) candidates[i].FARAchieved);
						stResult.userId.add(idList.get((int) (candidates[i].BIRInArray)));
					}
					if (numberOfResults.value > 1) {
						long mathWork1 = candidates[0].FARAchieved;
						long mathWork2 = candidates[1].FARAchieved;
						if ((mathWork1 - mathWork2) < 3000) {
							continue;
						}
					}
				}

				stResult.authenticated = true;

				break;
			}

			Ps_Sample_Apl_Java_NotifyResult_Identify(stResult);

		} catch (Exception e) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "run", e);
			}
		}

		return;

	}
}
