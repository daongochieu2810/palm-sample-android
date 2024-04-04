/*
 * PsMessageDialog.java
 *
 *	All Rights Reserved, Copyright(c) FUJITSU FRONTECH LIMITED 2021
 */

package com.fujitsu.frontech.palmsecure_sample;

import android.content.Context;

import com.fujitsu.frontech.palmsecure.JAVA_PvAPI_ErrorInfo;

public class PsMessageDialog {

	private static final String NEW_LINE_CODE = System.getProperty("line.separator");

	private static String ErrorFormat = null;

	public static String Ps_Sample_Apl_Java_ShowErrorDialog(Context context, JAVA_PvAPI_ErrorInfo errorInfo) {

		if (ErrorFormat == null) {
			// Error title
			ErrorFormat = context.getResources().getString(R.string.LibErrorTitle)
					+ NEW_LINE_CODE;
			// Error level
			ErrorFormat += context.getResources().getString(R.string.LibErrorLevel)
					+ " : 0x%02x" + NEW_LINE_CODE;
			// Error code
			ErrorFormat += context.getResources().getString(R.string.LibErrorCode)
					+ " : 0x%02x" + NEW_LINE_CODE;
			// Error detail
			ErrorFormat += context.getResources().getString(R.string.LibErrorDetail)
					+ " : 0x%08x" + NEW_LINE_CODE;
			ErrorFormat += "ErrorInfo1    : 0x%08x" + NEW_LINE_CODE;
			ErrorFormat += "ErrorInfo2    : 0x%08x" + NEW_LINE_CODE;
			ErrorFormat += "ErrorInfo3[0] : 0x%08x" + NEW_LINE_CODE;
			ErrorFormat += "ErrorInfo3[1] : 0x%08x" + NEW_LINE_CODE;
			ErrorFormat += "ErrorInfo3[2] : 0x%08x" + NEW_LINE_CODE;
			ErrorFormat += "ErrorInfo3[3] : 0x%08x" + NEW_LINE_CODE;
		}

		String message = String.format(ErrorFormat,
				errorInfo.ErrorLevel,
				errorInfo.ErrorCode,
				errorInfo.ErrorDetail,
				errorInfo.ErrorInfo1,
				errorInfo.ErrorInfo2,
				errorInfo.ErrorInfo3[0],
				errorInfo.ErrorInfo3[1],
				errorInfo.ErrorInfo3[2],
				errorInfo.ErrorInfo3[3]);

		return message;
	}

	public static String Ps_Sample_Apl_Java_ShowErrorDialog(int errNumber) {

		String message = "PalmSecureException";
		message += NEW_LINE_CODE;
		message += "Error No: " + errNumber;

		return message;
	}
}
