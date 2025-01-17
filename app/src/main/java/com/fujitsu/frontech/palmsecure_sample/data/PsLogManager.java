/*
 * PsLogManager.java
 *
 *	All Rights Reserved, Copyright(c) FUJITSU FRONTECH LIMITED 2021
 */
package com.fujitsu.frontech.palmsecure_sample.data;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.fujitsu.frontech.palmsecure_sample.BuildConfig;
import com.fujitsu.frontech.palmsecure_sample.exception.PsAplException;
import com.fujitsu.frontech.palmsecure_sample.R;
import com.fujitsu.frontech.palmsecure_sample.xml.PsFileAccessorIni;

import static com.fujitsu.frontech.palmsecure.util.PalmSecureConstant.SYSTEM_EXCEPTION;


public class PsLogManager {
	private static final String TAG = "PsLogManager";
	private static final String LOG_DIR = "Log";
	private static final String SILHOUETTE_DIR_ENROLL = "Enroll";
	private static final String SILHOUETTE_DIR_CAPTURE = "Capture";
	private static final String SILHOUETTE_DIR_MATCH = "Match";
	private static final String LOG_FILE = "Result.csv";
	private static final String DELIMITER = ",";
	private static final String SILHOUETTE_FILE_EXT = ".bmp";
	private static PsLogManager logManager = new PsLogManager();

	public static PsLogManager GetInstance() {
		return logManager;
	}


	public String Ps_Sample_Apl_Java_OutputSilhouette(String logDir, long sensorType, long dataType,
									String kind, byte[] silhouetteLog) throws PsAplException {

		String directory = new String();
		directory = logDir + File.separator;
		if (kind.equalsIgnoreCase("E")) {
			directory += SILHOUETTE_DIR_ENROLL;
		} else if (kind.equalsIgnoreCase("C")) {
			directory += SILHOUETTE_DIR_CAPTURE;
		} else {
			directory += SILHOUETTE_DIR_MATCH;
		}
		File silhouetteDir = new File(directory);
		if (!silhouetteDir.exists()) {
			silhouetteDir.mkdirs();
		}

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String date = format.format(cal.getTime());

		String path = silhouetteDir.getAbsolutePath();
		path += File.separator;
		path += Long.toString(sensorType);
		path += dataType;
		path += "_";
		path += date;
		path += SILHOUETTE_FILE_EXT;

		File silhouetteFile = new File(path);
		FileOutputStream outStream = null;
		FileChannel outChannel = null;;
		try {
			outStream = new FileOutputStream(silhouetteFile);
			outChannel = outStream.getChannel();
			ByteBuffer byteBuff = ByteBuffer.wrap(silhouetteLog);
			outChannel.write(byteBuff);
		} catch (FileNotFoundException e) {
			PsAplException pae = new PsAplException(R.string.AplErrorBmpSave);
			pae.setErrorMsgKey(SYSTEM_EXCEPTION);
			throw pae;
		} catch (IOException io) {
			PsAplException pae = new PsAplException(R.string.AplErrorBmpSave);
			pae.setErrorMsgKey(SYSTEM_EXCEPTION);
			throw pae;
		} finally {
			try {
				if (outChannel != null) {
					outChannel.close();
				}
				if (outStream != null) {
					outStream.close();
				}
			} catch(Exception e) {
			}
		}

		return silhouetteFile.getName();
	}


	public void Ps_Sample_Apl_Java_WriteLog(String logDirName, long sensorType, long dataType, String kind,
			boolean result, int retryNum, String silhouette, ArrayList<String> idList, ArrayList<Integer> scoreList) throws PsAplException {

		String log = new String();

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:dd");
		String date = format.format(cal.getTime());

		log = date + DELIMITER;
		log += Long.toString(sensorType) + DELIMITER;
		log += dataType + DELIMITER;
		log += kind + DELIMITER;

		if ( result == false) {
			log += "NG"  + DELIMITER;
		} else {
			log += "OK"  + DELIMITER;
		}

		log += retryNum + DELIMITER;
		log += silhouette;

		String id = new String();
		int i = 0;
		for (i = 0; i < idList.size(); i++) {
			id += DELIMITER;
			id += idList.get(i);
			if (scoreList.size() > i) {
				id += "(" + scoreList.get(i).toString() + ")";
			}
		}

		log += id + "\r\n";

		if (logDirName.compareTo("") == 0) {
			logDirName = LOG_DIR;
		}

		File LogDir = new File(logDirName);
		if (!LogDir.exists()) {
			LogDir.mkdir();
		}

		String path = LogDir.getAbsolutePath();
		path += File.separator;
		path += LOG_FILE;
		File logFile = new File(path);
		FileWriter fw = null;
		try {
			fw = new FileWriter(logFile, true);
			fw.write(log);
		} catch(IOException e) {
			Log.e(TAG, "Ps_Sample_Apl_Java_WriteLog", e);
			PsAplException pae = new PsAplException(R.string.AplErrorLogFileWrite);
			pae.setErrorMsgKey(SYSTEM_EXCEPTION);
			throw pae;
		} finally {
			try {
				fw.close();
			} catch(Exception e) {
			}
		}
	}
}
