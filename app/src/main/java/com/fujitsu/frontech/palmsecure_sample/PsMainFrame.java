/*
 * PsMainFrame.java
 *
 *	All Rights Reserved, Copyright(c) FUJITSU FRONTECH LIMITED 2021
 */

package com.fujitsu.frontech.palmsecure_sample;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.fujitsu.frontech.palmsecure.JAVA_BioAPI_GUI_BITMAP;
import com.fujitsu.frontech.palmsecure.util.PalmSecureConstant;
import com.fujitsu.frontech.palmsecure.util.PalmSecureException;
import com.fujitsu.frontech.palmsecure_sample.data.PsDataManager;
import com.fujitsu.frontech.palmsecure_sample.data.PsThreadResult;
import com.fujitsu.frontech.palmsecure_sample.data.PsLogManager;
import com.fujitsu.frontech.palmsecure_sample.event.PsBusinessListener;
import com.fujitsu.frontech.palmsecure_sample.event.PsServiceClient;
import com.fujitsu.frontech.palmsecure_sample.exception.PsAplException;
import com.fujitsu.frontech.palmsecure_sample.service.PsService;
import com.fujitsu.frontech.palmsecure_sample.service.PsServiceHelper;
import com.fujitsu.frontech.palmsecure_sample.xml.PsFileAccessorIni;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

@SuppressLint("ParcelCreator")
public class PsMainFrame implements Parcelable, PsBusinessListener {

	private static final String TAG = "PsMainFrame";
	private static int PS_REGISTER_MAX = 5000;
	public static final int[] PS_GEXTENDED_DATA_TYPE = {2, 3};
	private static final int USB_DEVICE_VENDOR_ID = 1221;
	private static final int PRODUCT_ID_PALMSECURE_F_PRO = 5414;
	private static final String ACTION_USB_PERMISSION = "com.fujitsu.frontech.palmsecure_sample.USB_PERMISSION";
	private static final int BTN_STATE_INIT = 0;
	private static final int BTN_STATE_WAITING = 1;
	private static final int BTN_STATE_ACTIVE = 2;

	private Activity mActivity;
	private TextView mGuidance;
	private TextView mWorkMessage;
	private TextView mTitle;
	private ListView mIdList;
	private EditText mId;
	private FloatingActionButton mFloating;
	private TextView mIdNum;
	private FloatingActionButton mEnrollBtn;
	private Button mDeleteBtn;
	private FloatingActionButton mVerifyBtn;
	private FloatingActionButton mIdentifyBtn;;
	private Button mCancelBtn;
	private Button mExitBtn;;
	private ImageView mImage;
	private TextView mIdListLbl;

	private ArrayAdapter<String> mArrayAdapter = null;
	private SoundPool mSp;
	private int mSound_ok;
	private int mSound_ng;
	private int mUsingGuideMode = 0;
	private long mUsingSensorType = 0;
	private long mUsingSensorExtKind = 0;
	private int mUsingGExtendedMode = 0;
	private int mUsingDataType = 0;
	private int mSensorNameId = 0;
	private boolean isIdentifyEnable = true;
	private boolean isSupportedSensor = true;

	private boolean mCancelFlg = false;
	private String mIdVal = "";
	private int mGuidanceVal = 0;
	private String mUserId = "";
	private boolean mError = false;
	private int mWorkMsgVal = 0;
	private int mCount = 0;
	private int mBtnState = BTN_STATE_INIT;
	private Bitmap mImageVal = null;
	private boolean mInitLibrary = false;
	private HashMap<String, UsbDevice> mChkDeviceList = null;

	private PsServiceClient mClient = null;
	private PsFileAccessorIni mIniAcs;
	private Boolean isFabVisible = false;

	public PsMainFrame(Activity activity) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "PsMainFrame");
		mActivity = activity;
		Ps_Sample_Apl_Java_InitAuthDataFile();

		mIniAcs = PsFileAccessorIni.GetInstance(mActivity.getApplicationContext());
		if (mIniAcs == null) {

			DialogInterface.OnClickListener listener;
			listener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mActivity.finish();
				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setCancelable(false);
			builder.setTitle("Error").setMessage("Read Error: " + PsFileAccessorIni.FileName);
			builder.setPositiveButton("OK", listener);
			builder.create().show();

			return;
		}

		mUsingGuideMode = mIniAcs.GetValueInteger(PsFileAccessorIni.GuideMode);
		//mUsingGExtendedMode = mIniAcs.GetValueInteger(PsFileAccessorIni.GExtendedMode);
		mUsingGExtendedMode = (int)PalmSecureConstant.JAVA_PvAPI_PRE_PROFILE_G_EXTENDED_MODE_2;
		if (mUsingGExtendedMode >= 1) {
			mUsingDataType = PS_GEXTENDED_DATA_TYPE[mUsingGExtendedMode-1];
		} else {
			mUsingDataType = mUsingGuideMode;
			PS_REGISTER_MAX = 1000;
		}

		mActivity.startService(new Intent(mActivity, PsService.class));

		Ps_Sample_Apl_Java(activity);
		Ps_Sample_Apl_Java_InitSound();
		Ps_Sample_Apl_Java_SetComponentEnabled(BTN_STATE_INIT);
	}

	public void Ps_Sample_Apl_Java(Activity activity) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java");
		mActivity = activity;
		ActionBar actionBar = mActivity.getActionBar();
		if (actionBar != null) actionBar.hide();
		mActivity.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		mActivity.setContentView(R.layout.ps_main_frame);

		Ps_Sample_Apl_Java_InitControls();
		Ps_Sample_Apl_Java_RestoreControls();

		if (!Ps_Sample_Apl_Java_ServiceConnect()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setCancelable(false);
			builder.setTitle("Caution").setMessage("Initialize Error");
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mActivity.finish();
						}
					});
			builder.create().show();
		}

	}

	private boolean Ps_Sample_Apl_Java_ServiceConnect() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_ServiceConnect");
		try {
			mClient = new PsServiceClient(this, mActivity);
		} catch (PsAplException e) {
			if (BuildConfig.DEBUG)
				Log.e(TAG, "PsServiceClient PsAplException ", e);
			return false;
		}
		return true;
	}

	private void Ps_Sample_Apl_Java_ServiceDisconnect() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_ServiceDisconnect");
		if (mClient != null) {
			mClient.Ps_Sample_Apl_Java_ServiceDisconnect();
			mClient = null;
		}
	}

	public void Ps_Sample_Apl_Java_onDestroy() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_onDestroy");
		if (mUsbReceiver != null) {
			mActivity.unregisterReceiver(mUsbReceiver);
			mUsbReceiver = null;
		}

		Ps_Sample_Apl_Java_ServiceDisconnect();
	}

	private void Ps_Sample_Apl_Java_InitAuthDataFile() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_InitAuthDataFile");
		FileOutputStream os = null;
		ZipInputStream zis = null;
		try {
			byte[] buf = new byte[1024];
			int size;
			zis = new ZipInputStream(mActivity.getResources().openRawResource(
					R.raw.authdatafile));
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {
				String[] tokens = ze.getName().split("/");
				os = mActivity.openFileOutput(tokens[tokens.length - 1],
						Context.MODE_PRIVATE);
				while ((size = zis.read(buf, 0, buf.length)) > 0) {
					os.write(buf, 0, size);
				}
			}
		} catch (Exception e) {
			if (BuildConfig.DEBUG)
				Log.e(TAG, e.getMessage());
		} finally {
			try {
				if (zis != null)
					zis.close();
				if (os != null)
					os.close();
			} catch (Exception e) {
				if (BuildConfig.DEBUG)
					Log.e(TAG, e.getMessage());
			}
		}
	}

	private void Ps_Sample_Apl_Java_InitSound() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_InitSound");
		mSp = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		mSound_ok = mSp.load(mActivity, R.raw.ok, 1);
		mSound_ng = mSp.load(mActivity, R.raw.ng, 1);
	}

	private void Ps_Sample_Apl_Java_PlayWave(boolean ok) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_PlayWave");
		int sid;
		if (ok)
			sid = mSound_ok;
		else
			sid = mSound_ng;
		mSp.play(sid, 1.0F, 1.0F, 0, 0, 1.0F);
	}

	private void Ps_Sample_Apl_Java_DisplayGuideBmp() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_DisplayGuideBmp");
		int rid;
		if (mUsingGuideMode == 0)
			rid = R.drawable.guideless;
		else
			rid = R.drawable.handguide;
		// mImage.setImageResource(rid);
		mImageVal = null;
	}

	private void Ps_Sample_Apl_Java_SetGuidance(int msgId, boolean error) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_SetGuidance: " + msgId);
		String guidance;
		if (msgId == 0)
			guidance = "";
		else
			guidance = mActivity.getResources().getString(msgId);
		mGuidance.setText(guidance);
		if (error)
			mGuidance.setTextColor(Color.RED);
		else
			mGuidance.setTextColor(Color.BLUE);
		mGuidanceVal = msgId;
		mError = error;
	}

	private void Ps_Sample_Apl_Java_SetGuidance(int msgId, boolean error,
			String userID) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_SetGuidance: " + msgId);
		String guidance;
		if (msgId == 0)
			guidance = "";
		else
			guidance = String.format(mActivity.getResources().getString(msgId), userID);
		mGuidance.setText(guidance);
		if (error)
			mGuidance.setTextColor(Color.RED);
		else
			mGuidance.setTextColor(Color.BLUE);
		mGuidanceVal = msgId;
		mError = error;
	}

	private void Ps_Sample_Apl_Java_SetWorkMessage(int msgId) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_SetWorkMessage: " + msgId);
		if (msgId == 0) {
			mWorkMessage.setText("");
			mWorkMessage.setBackgroundColor(Color.WHITE);
		} else {
			mWorkMessage.setText(mActivity.getResources().getString(msgId));
			mWorkMessage.setBackgroundColor(Color.GREEN);
		}
		mWorkMsgVal = msgId;
		mCount = 0;
	}

	private void Ps_Sample_Apl_Java_SetWorkMessage(int msgId, int count) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_SetWorkMessage: " + msgId);
		if (msgId == 0) {
			mWorkMessage.setText("");
			mWorkMessage.setBackgroundColor(Color.WHITE);
		} else {
			mWorkMessage.setText(String.format(mActivity.getResources().getString(msgId), count));
			mWorkMessage.setBackgroundColor(Color.GREEN);
		}
		mWorkMsgVal = msgId;
		mCount = count;
	}

	private void Ps_Sample_Apl_Java_SetWorkMessage(int msgId, int count, int max) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_SetWorkMessage: " + msgId);
		if (msgId == 0) {
			mWorkMessage.setText("");
			mWorkMessage.setBackgroundColor(Color.WHITE);
		} else {
			mWorkMessage.setText(String.format(mActivity.getResources().getString(msgId), count, max));
			mWorkMessage.setBackgroundColor(Color.GREEN);
		}
		mWorkMsgVal = msgId;
		mCount = count;
	}

	private void hideFab() {
		mEnrollBtn.hide();
		mIdentifyBtn.hide();
		mVerifyBtn.hide();

		isFabVisible = false;
	}

	private void Ps_Sample_Apl_Java_InitControls() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_InitControls");
		mGuidance = (TextView) mActivity.findViewById(R.id.Guidance);
		mWorkMessage = (TextView) mActivity.findViewById(R.id.WorkMessage);
		mTitle = (TextView) mActivity.findViewById(R.id.Title);
		mIdList = (ListView) mActivity.findViewById(R.id.IdList);
		TextInputLayout textInputLayout = (TextInputLayout) mActivity.findViewById(R.id.Id);
		mId = textInputLayout.getEditText();
		mFloating = (FloatingActionButton) mActivity.findViewById(R.id.mainFloating);
		mIdNum = (TextView) mActivity.findViewById(R.id.IdNum);
		mEnrollBtn = (FloatingActionButton) mActivity.findViewById(R.id.EnrollBtn);
		mDeleteBtn = (Button) mActivity.findViewById(R.id.DeleteBtn);
		mVerifyBtn = (FloatingActionButton) mActivity.findViewById(R.id.VerifyBtn);
		mIdentifyBtn = (FloatingActionButton) mActivity.findViewById(R.id.IdentifyBtn);
		mCancelBtn = (Button) mActivity.findViewById(R.id.CancelBtn);
		mExitBtn = (Button) mActivity.findViewById(R.id.ExitBtn);
		mImage = (ImageView) mActivity.findViewById(R.id.Image);
		mIdListLbl = (TextView) mActivity.findViewById(R.id.IdListLbl);

		mEnrollBtn.setVisibility(View.GONE);
		mIdentifyBtn.setVisibility(View.GONE);
		mVerifyBtn.setVisibility(View.GONE);

		mFloating.setOnClickListener(view -> {
			if (!isFabVisible) {
				mEnrollBtn.show();
				mIdentifyBtn.show();
				mVerifyBtn.show();

				isFabVisible = true;
			} else {
				hideFab();
			}
		});

		mEnrollBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hideFab();
				Ps_Sample_Apl_Java_ActionEnroll();
			}
		});
		mDeleteBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Ps_Sample_Apl_Java_ActionDelete();
			}
		});
		mVerifyBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hideFab();
				Ps_Sample_Apl_Java_ActionVerify();
			}
		});
		mIdentifyBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hideFab();
				Ps_Sample_Apl_Java_ActionIdentify();
			}
		});
		mCancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Ps_Sample_Apl_Java_ActionCancel();
			}
		});
		mExitBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Ps_Sample_Apl_Java_ActionExit();
			}
		});
		mIdListLbl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mArrayAdapter = null;
				Ps_Sample_Apl_Java_InitIdList(false);
			}
		});
		mId.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
				if (BuildConfig.DEBUG)
					Log.d(TAG, "afterTextChanged");
				mIdVal = mId.getText().toString();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
	}

	private void Ps_Sample_Apl_Java_InitIdList(boolean initIdList) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_InitIdList");
		if (mArrayAdapter == null || initIdList) {
			mArrayAdapter = new ArrayAdapter<String>(mActivity,
					com.google.android.material.R.layout.support_simple_spinner_dropdown_item);
			PsDataManager dataMng = new PsDataManager(mActivity,
					mUsingSensorType, mUsingDataType);
			ArrayList<String> idList = new ArrayList<String>();
			try {
				dataMng.convertDBToBioAPI_Data_All(idList);
				mArrayAdapter.addAll(idList);
			} catch (PsAplException | PalmSecureException e) {
			}
		}
		mIdList.setAdapter(mArrayAdapter);
		mIdList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				if (mBtnState == BTN_STATE_WAITING)
					mId.setText((String) ((ListView) parent)
							.getItemAtPosition(position));
			}
		});
		Ps_Sample_Apl_Java_UpdateIdList();
	}

	private void Ps_Sample_Apl_Java_UpdateIdList() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_UpdateIdList");
		mArrayAdapter.sort(null);
		mIdNum.setText(String.valueOf(mArrayAdapter.getCount()));
	}

	private void Ps_Sample_Apl_Java_RestoreControls() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_Restore");
		Ps_Sample_Apl_Java_InitIdList(false);
		Ps_Sample_Apl_Java_SetComponentEnabled(mBtnState);
		mId.setText(mIdVal);
		mId.requestFocus();
		mId.selectAll();
		Ps_Sample_Apl_Java_SetWorkMessage(mWorkMsgVal, mCount);
		Ps_Sample_Apl_Java_SetGuidance(mGuidanceVal, mError, mUserId);
		if (mImageVal != null) {
			// mImage.setImageBitmap(mImageVal);
		} else {
			Ps_Sample_Apl_Java_DisplayGuideBmp();
		}
	}

	private void Ps_Sample_Apl_Java_InitState() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_InitState");
		Ps_Sample_Apl_Java_SetComponentEnabled(BTN_STATE_WAITING);
		mId.requestFocus();
		mId.selectAll();
		mCancelFlg = false;
	}

	private void Ps_Sample_Apl_Java_SetComponentEnabled(int state) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_SetComponentEnabled");

		mBtnState = state;

		switch (state) {
		case BTN_STATE_WAITING:
			mEnrollBtn.setEnabled(true);
			mDeleteBtn.setEnabled(true);
			mVerifyBtn.setEnabled(true);
			mIdentifyBtn.setEnabled(true);
			mCancelBtn.setEnabled(false);
			mExitBtn.setEnabled(true);
			mId.setEnabled(true);
			break;
		case BTN_STATE_ACTIVE:
			mEnrollBtn.setEnabled(false);
			mDeleteBtn.setEnabled(false);
			mVerifyBtn.setEnabled(false);
			mIdentifyBtn.setEnabled(false);
			mCancelBtn.setEnabled(true);
			mExitBtn.setEnabled(false);
			mId.setEnabled(false);
			break;
		default:
			mEnrollBtn.setEnabled(false);
			mDeleteBtn.setEnabled(false);
			mVerifyBtn.setEnabled(false);
			mIdentifyBtn.setEnabled(false);
			mCancelBtn.setEnabled(false);
			mExitBtn.setEnabled(true);
			mId.setEnabled(false);
			break;
		}
		if(!isIdentifyEnable){
			mIdentifyBtn.setEnabled(false);
		}
		if(!isSupportedSensor){
			mEnrollBtn.setEnabled(false);
			mDeleteBtn.setEnabled(false);
			mVerifyBtn.setEnabled(false);
			mIdentifyBtn.setEnabled(false);
			mCancelBtn.setEnabled(false);
			mExitBtn.setEnabled(true);
			mId.setEnabled(false);
		}
	}

	private void Ps_Sample_Apl_Java_InitLibrary() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_InitLibrary");

		if (mInitLibrary == false && mClient != null) {
			mInitLibrary = true;
			mClient.Ps_Sample_Apl_Java_ServiceRequest_InitLibrary(
					mIniAcs.GetValueString(PsFileAccessorIni.ApplicationKey),
					mUsingGuideMode,
					mUsingDataType);
		}
	}

	private void Ps_Sample_Apl_Java_TermLibrary() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_TermLibrary");

		mClient.Ps_Sample_Apl_Java_ServiceRequest_TermLibrary();
		mInitLibrary = false;
	}

	private void Ps_Sample_Apl_Java_ActionEnroll() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_ActionEnroll");
		Ps_Sample_Apl_Java_SetWorkMessage(R.string.WorkEnrollStart);
		Ps_Sample_Apl_Java_SetGuidance(0, false);
		Ps_Sample_Apl_Java_DisplayGuideBmp();
		mUserId = mId.getText().toString();
		if (mUserId.compareTo("") == 0) {
			Ps_Sample_Apl_Java_SetGuidance(R.string.IllegalId, true);
			Ps_Sample_Apl_Java_SetWorkMessage(0);
			Ps_Sample_Apl_Java_InitState();
			return;
		}
		if (mArrayAdapter.getPosition(mUserId) >= 0) {
			Ps_Sample_Apl_Java_SetGuidance(R.string.RegistId, true);
			Ps_Sample_Apl_Java_SetWorkMessage(0);
			Ps_Sample_Apl_Java_InitState();
			return;
		}
		if (mArrayAdapter.getCount() >= PS_REGISTER_MAX) {
			Ps_Sample_Apl_Java_SetGuidance(R.string.MaxOver, true);
			Ps_Sample_Apl_Java_SetWorkMessage(0);
			Ps_Sample_Apl_Java_InitState();
			return;
		}
		Ps_Sample_Apl_Java_SetComponentEnabled(BTN_STATE_ACTIVE);
		mClient.Ps_Sample_Apl_Java_ServiceRequest_Enroll(
				mUserId,
				mIniAcs.GetValueInteger(PsFileAccessorIni.NumberOfRetry),
				mIniAcs.GetValueInteger(PsFileAccessorIni.SleepTime));
		mCancelBtn.requestFocus();
	}

	private void Ps_Sample_Apl_Java_ActionDelete() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_ActionDelete");
		Ps_Sample_Apl_Java_SetWorkMessage(R.string.WorkDelete);
		Ps_Sample_Apl_Java_SetGuidance(0, false);
		mUserId = mId.getText().toString();
		if (mUserId.compareTo("") == 0) {
			Ps_Sample_Apl_Java_SetGuidance(R.string.IllegalId, true);
			Ps_Sample_Apl_Java_SetWorkMessage(0);
			return;
		}
		if (mArrayAdapter.getPosition(mUserId) < 0) {
			Ps_Sample_Apl_Java_SetGuidance(R.string.UnregistId, true);
			Ps_Sample_Apl_Java_SetWorkMessage(0);
			return;
		}
		Ps_Sample_Apl_Java_SetComponentEnabled(BTN_STATE_ACTIVE);
		mCancelBtn.setEnabled(false);
		try {
			PsDataManager dataMng = new PsDataManager(mActivity,
					mUsingSensorType, mUsingDataType);
			dataMng.deleteDBToBioAPI_Data(mUserId);
			mArrayAdapter.remove(mUserId);
			Ps_Sample_Apl_Java_SetGuidance(R.string.Delete, false);
		} catch (PsAplException e) {
			if (e.getErrorMsgKey() == R.string.AplErrorSystemError) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
				builder.setCancelable(false);
				builder.setTitle("Caution").setMessage(e.getErrorMsgKey());
				builder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
				builder.create().show();
			}
			else {
				Ps_Sample_Apl_Java_SetGuidance(e.getErrorMsgKey(), true);
			}
		}
		Ps_Sample_Apl_Java_InitState();
		Ps_Sample_Apl_Java_UpdateIdList();
		Ps_Sample_Apl_Java_SetWorkMessage(0);
	}

	private void Ps_Sample_Apl_Java_ActionVerify() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_ActionVerify");
		Ps_Sample_Apl_Java_SetWorkMessage(R.string.WorkVerifyStart);
		Ps_Sample_Apl_Java_SetGuidance(0, false);
		Ps_Sample_Apl_Java_DisplayGuideBmp();
		mUserId = mId.getText().toString();
		if (mUserId.compareTo("") == 0) {
			Ps_Sample_Apl_Java_SetGuidance(R.string.IllegalId, true);
			Ps_Sample_Apl_Java_SetWorkMessage(0);
			return;
		}
		if (mArrayAdapter.getPosition(mUserId) < 0) {
			Ps_Sample_Apl_Java_SetGuidance(R.string.UnregistId, true);
			Ps_Sample_Apl_Java_SetWorkMessage(0);
			return;
		}
		Ps_Sample_Apl_Java_SetComponentEnabled(BTN_STATE_ACTIVE);
		mClient.Ps_Sample_Apl_Java_ServiceRequest_Verify(
				mUserId,
				mIniAcs.GetValueInteger(PsFileAccessorIni.NumberOfRetry),
				mIniAcs.GetValueInteger(PsFileAccessorIni.SleepTime));
		mCancelBtn.requestFocus();
	}

	private void Ps_Sample_Apl_Java_ActionIdentify() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_ActionIdentify");
		Ps_Sample_Apl_Java_SetWorkMessage(R.string.WorkIdentifyStart);
		Ps_Sample_Apl_Java_SetGuidance(0, false);
		Ps_Sample_Apl_Java_DisplayGuideBmp();
		if (mArrayAdapter.getCount() <= 0) {
			Ps_Sample_Apl_Java_SetGuidance(R.string.Unregist, true);
			Ps_Sample_Apl_Java_SetWorkMessage(0);
			return;
		}
		Ps_Sample_Apl_Java_SetComponentEnabled(BTN_STATE_ACTIVE);
		mClient.Ps_Sample_Apl_Java_ServiceRequest_Identify(
				mIniAcs.GetValueInteger(PsFileAccessorIni.NumberOfRetry),
				mIniAcs.GetValueInteger(PsFileAccessorIni.SleepTime),
				mIniAcs.GetValueInteger(PsFileAccessorIni.MaxResults));
		mCancelBtn.requestFocus();
	}

	private void Ps_Sample_Apl_Java_ActionCancel() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_ActionCancel");
		mCancelFlg = true;
		mClient.Ps_Sample_Apl_Java_ServiceRequest_Cancel();
		Ps_Sample_Apl_Java_SetComponentEnabled(BTN_STATE_WAITING);
	}

	private void Ps_Sample_Apl_Java_ActionExit() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_ActionExit");
		Ps_Sample_Apl_Java_TermLibrary();
		Ps_Sample_Apl_Java_ServiceDisconnect();
		mActivity.stopService(new Intent(mActivity, PsService.class));
		mActivity.finish();
	}

	@Override
	public void Ps_Sample_Apl_Java_NotifyWorkMessage(int messageId) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_NotifyWorkMessage");
		Ps_Sample_Apl_Java_SetWorkMessage(messageId);
	}

	@Override
	public void Ps_Sample_Apl_Java_NotifyWorkMessage(int messageId, int count) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_NotifyWorkMessage");
		Ps_Sample_Apl_Java_SetWorkMessage(messageId, count);
	}

	@Override
	public void Ps_Sample_Apl_Java_NotifyWorkMessage(int messageId, int count, int number) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_NotifyWorkMessage");
		Ps_Sample_Apl_Java_SetWorkMessage(messageId, count, number);
	}

	@Override
	public void Ps_Sample_Apl_Java_NotifyGuidance(int guidanceId, boolean error) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_NotifyGuidance");
		Ps_Sample_Apl_Java_SetGuidance(guidanceId, error);
	}

	public void Ps_Sample_Apl_Java_NotifySilhouette(JAVA_BioAPI_GUI_BITMAP bm) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_NotifySilhouette");
		Bitmap bitmap = BitmapFactory.decodeByteArray(bm.Bitmap.Data, 0,
				(int) bm.Bitmap.Length);
		Matrix matrix = new Matrix();
		matrix.setScale(-1.0f, 1.0f);
		mImageVal = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		// mImage.setImageBitmap(mImageVal);
	}

	@Override
	public void Ps_Sample_Apl_Java_NotifyResult_Enroll(PsThreadResult stResult,
			int enrollscore) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_NotifyResult_Enroll");

		boolean enrollResult = false;
		PsLogManager logMng = PsLogManager.GetInstance();

		if (stResult.result != PalmSecureConstant.JAVA_BioAPI_OK
				|| stResult.authenticated == false || mCancelFlg == true) {
			if (mCancelFlg == true) {
				Ps_Sample_Apl_Java_SetGuidance(R.string.EnrollCancel, false);
			} else {
				Ps_Sample_Apl_Java_SetGuidance(R.string.EnrollNg, true);
			}
		} else if (enrollscore > PalmSecureConstant.JAVA_PvAPI_REGIST_SCORE_QUALITY_2
				|| stResult.farAchieved.get(0) < 3000) {
			enrollResult = true;
			Ps_Sample_Apl_Java_SetGuidance(R.string.EnrollRetry, false);
		} else {
			enrollResult = true;
			// mUserId = stResult.userId.get(0);
			mArrayAdapter.add(mUserId);
			Ps_Sample_Apl_Java_SetGuidance(R.string.EnrollOk, false);
			Ps_Sample_Apl_Java_UpdateIdList();
		}

		//Output a silhouette image
		String silhouetteFile = new String();

		String sLogPath = new String();
		String str = mActivity.getExternalFilesDir(null).getPath();
		sLogPath = str + File.separator + mIniAcs.GetValueString(PsFileAccessorIni.LogFolderPath);

		if (mIniAcs.GetValueInteger(PsFileAccessorIni.SilhouetteMode) == 1
				&& stResult.result == PalmSecureConstant.JAVA_BioAPI_OK
				&& stResult.info != null
				&& mCancelFlg != true)
		{
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "dir=" + str);
				Log.d(TAG, "mUsingSensorType=" + mUsingSensorType);
				Log.d(TAG, "mUsingDataType=" + mUsingDataType);
			}

			try {
				silhouetteFile = logMng.Ps_Sample_Apl_Java_OutputSilhouette(
						sLogPath,
						mUsingSensorType,
						mUsingDataType,
						"E",
						stResult.info);
			} catch (PsAplException pae) {
                String msg = PsMessageDialog.Ps_Sample_Apl_Java_ShowErrorDialog(pae.getErrorMsgKey());

                DialogInterface.OnClickListener listener;
                listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Ps_Sample_Apl_Java_TermLibrary();
                        mActivity.finish();
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setCancelable(false);
                builder.setTitle("Caution").setMessage(msg);
                builder.setPositiveButton("OK", listener);
                builder.create().show();
            }
		}

		if (BuildConfig.DEBUG) {
			Log.e(TAG, "Output a silhouette image after");
		}

		if (mIniAcs.GetValueInteger(PsFileAccessorIni.LogMode) == 1
				&& stResult.result == PalmSecureConstant.JAVA_BioAPI_OK
				&& mCancelFlg != true)
		{
			// Output log
			try {
				logMng.Ps_Sample_Apl_Java_WriteLog(
						sLogPath,
						mUsingSensorType,
						mUsingDataType,
						"E",
						enrollResult,
						stResult.retryCnt,
						silhouetteFile,
						stResult.userId,
						stResult.farAchieved);
			} catch (PsAplException pae) {
                String msg = PsMessageDialog.Ps_Sample_Apl_Java_ShowErrorDialog(pae.getErrorMsgKey());

                DialogInterface.OnClickListener listener;
                listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Ps_Sample_Apl_Java_TermLibrary();
                        mActivity.finish();
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setCancelable(false);
                builder.setTitle("Caution").setMessage(msg);
                builder.setPositiveButton("OK", listener);
                builder.create().show();
            }
		}

		Ps_Sample_Apl_Java_InitState();
		Ps_Sample_Apl_Java_SetWorkMessage(0);
		Ps_Sample_Apl_Java_ShowErrorDialog(stResult);

		return;
	}

	@Override
	public void Ps_Sample_Apl_Java_NotifyResult_Verify(PsThreadResult stResult) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_NotifyResult_Verify");

		PsLogManager logMng = PsLogManager.GetInstance();
		boolean verifyResult = false;

		mUserId = stResult.userId.get(0);
		if (stResult.result == PalmSecureConstant.JAVA_BioAPI_OK
				&& stResult.authenticated == true && mCancelFlg != true) {
			Ps_Sample_Apl_Java_PlayWave(true);
			Ps_Sample_Apl_Java_SetGuidance(R.string.VerifyOk, false, mUserId);
			verifyResult = true;
		} else if (mCancelFlg == true) {
			Ps_Sample_Apl_Java_SetGuidance(R.string.VerifyCancel, false);
		} else {
			Ps_Sample_Apl_Java_PlayWave(false);
			Ps_Sample_Apl_Java_SetGuidance(R.string.VerifyNg, true, mUserId);
		}

		//Output a silhouette image
		String silhouetteFile = new String();

		String sLogPath = new String();
		String str = mActivity.getExternalFilesDir(null).getPath();
		sLogPath = str + File.separator + mIniAcs.GetValueString(PsFileAccessorIni.LogFolderPath);

		if (mIniAcs.GetValueInteger(PsFileAccessorIni.SilhouetteMode) == 1
				&& stResult.result == PalmSecureConstant.JAVA_BioAPI_OK
				&& stResult.info != null
				&& mCancelFlg != true)
		{
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "dir=" + str);
				Log.d(TAG, "mUsingSensorType=" + mUsingSensorType);
				Log.d(TAG, "mUsingDataType=" + mUsingDataType);
			}

			try {
				silhouetteFile = logMng.Ps_Sample_Apl_Java_OutputSilhouette(
						sLogPath,
						mUsingSensorType,
						mUsingDataType,
						"V",
						stResult.info);
			} catch (PsAplException pae) {
				String msg = PsMessageDialog.Ps_Sample_Apl_Java_ShowErrorDialog(pae.getErrorMsgKey());

				DialogInterface.OnClickListener listener;
				listener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Ps_Sample_Apl_Java_TermLibrary();
						mActivity.finish();
					}
				};

				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
				builder.setCancelable(false);
				builder.setTitle("Caution").setMessage(msg);
				builder.setPositiveButton("OK", listener);
				builder.create().show();
			}
		}

		if (BuildConfig.DEBUG) {
			Log.e(TAG, "Output a silhouette image after");
		}

		if (mIniAcs.GetValueInteger(PsFileAccessorIni.LogMode) == 1
				&& stResult.result == PalmSecureConstant.JAVA_BioAPI_OK
				&& mCancelFlg != true)
		{
			// Output log
			try {
				logMng.Ps_Sample_Apl_Java_WriteLog(
						sLogPath,
						mUsingSensorType,
						mUsingDataType,
						"V",
						verifyResult,
						stResult.retryCnt,
						silhouetteFile,
						stResult.userId,
						stResult.farAchieved);
			} catch (PsAplException pae) {
                String msg = PsMessageDialog.Ps_Sample_Apl_Java_ShowErrorDialog(pae.getErrorMsgKey());

                DialogInterface.OnClickListener listener;
                listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Ps_Sample_Apl_Java_TermLibrary();
                        mActivity.finish();
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setCancelable(false);
                builder.setTitle("Caution").setMessage(msg);
                builder.setPositiveButton("OK", listener);
                builder.create().show();
            }
		}

		Ps_Sample_Apl_Java_InitState();
		Ps_Sample_Apl_Java_SetWorkMessage(0);
		Ps_Sample_Apl_Java_ShowErrorDialog(stResult);

		return;
	}

	@Override
	public void Ps_Sample_Apl_Java_NotifyResult_Identify(PsThreadResult stResult) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_NotifyResult_Identify");

		PsLogManager logMng = PsLogManager.GetInstance();
		boolean identifyResult = false;

		if (stResult.result == PalmSecureConstant.JAVA_BioAPI_OK
				&& stResult.farAchieved.size() > 0
				&& stResult.authenticated == true && mCancelFlg != true) {
			Ps_Sample_Apl_Java_PlayWave(true);
			mUserId = stResult.userId.get(0);
			Ps_Sample_Apl_Java_SetGuidance(R.string.IdentifyOk, false, mUserId);
			identifyResult = true;
		} else if (mCancelFlg == true) {
			Ps_Sample_Apl_Java_SetGuidance(R.string.IdentifyCancel, false);
		} else {
			Ps_Sample_Apl_Java_PlayWave(false);
			Ps_Sample_Apl_Java_SetGuidance(R.string.IdentifyNg, true);
		}

		//Output a silhouette image
		String silhouetteFile = new String();

		String sLogPath = new String();
		String str = mActivity.getExternalFilesDir(null).getPath();
		sLogPath = str + File.separator + mIniAcs.GetValueString(PsFileAccessorIni.LogFolderPath);

		if (mIniAcs.GetValueInteger(PsFileAccessorIni.SilhouetteMode) == 1
				&& stResult.result == PalmSecureConstant.JAVA_BioAPI_OK
				&& stResult.info != null
				&& mCancelFlg != true)
		{
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "dir=" + str);
				Log.d(TAG, "mUsingSensorType=" + mUsingSensorType);
				Log.d(TAG, "mUsingDataType=" + mUsingDataType);
			}

			try {
				silhouetteFile = logMng.Ps_Sample_Apl_Java_OutputSilhouette(
						sLogPath,
						mUsingSensorType,
						mUsingDataType,
						"I",
						stResult.info);
			} catch (PsAplException pae) {
                String msg = PsMessageDialog.Ps_Sample_Apl_Java_ShowErrorDialog(pae.getErrorMsgKey());

                DialogInterface.OnClickListener listener;
                listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Ps_Sample_Apl_Java_TermLibrary();
                        mActivity.finish();
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setCancelable(false);
                builder.setTitle("Caution").setMessage(msg);
                builder.setPositiveButton("OK", listener);
                builder.create().show();
			}
		}

		if (BuildConfig.DEBUG) {
			Log.e(TAG, "Output a silhouette image after");
		}

		if (mIniAcs.GetValueInteger(PsFileAccessorIni.LogMode) == 1
				&& stResult.result == PalmSecureConstant.JAVA_BioAPI_OK
				&& mCancelFlg != true)
		{
			// Output log
			try {
				logMng.Ps_Sample_Apl_Java_WriteLog(
						sLogPath,
						mUsingSensorType,
						mUsingDataType,
						"I",
						identifyResult,
						stResult.retryCnt,
						silhouetteFile,
						stResult.userId,
						stResult.farAchieved);
			} catch (PsAplException pae) {
                String msg = PsMessageDialog.Ps_Sample_Apl_Java_ShowErrorDialog(pae.getErrorMsgKey());

                DialogInterface.OnClickListener listener;
                listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Ps_Sample_Apl_Java_TermLibrary();
                        mActivity.finish();
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setCancelable(false);
                builder.setTitle("Caution").setMessage(msg);
                builder.setPositiveButton("OK", listener);
                builder.create().show();
            }
		}

		Ps_Sample_Apl_Java_InitState();
		Ps_Sample_Apl_Java_SetWorkMessage(0);
		Ps_Sample_Apl_Java_ShowErrorDialog(stResult);

		return;
	}

	@Override
	public void Ps_Sample_Apl_Java_NotifyResult_Cancel(PsThreadResult stResult) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_NotifyResult_Cancel");
	}

	@Override
	public void Ps_Sample_Apl_Java_NotifyResult_InitLibrary(PsThreadResult stResult, long type, long extKind) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_NotifyResult_InitLibrary: type=" + type + ", extKind=" + extKind);

		mUsingSensorType = type;
		mUsingSensorExtKind = extKind;

		if (stResult.result == PalmSecureConstant.JAVA_BioAPI_OK) {

			switch ((int )mUsingSensorType) {
				case (int) PalmSecureConstant.JAVA_PvAPI_INFO_SENSOR_TYPE_2:
					if (mUsingSensorExtKind == PalmSecureConstant.JAVA_PvAPI_INFO_SENSOR_MODE_COMPATIBLE) {
						mSensorNameId = R.string.SensorName1_Compati;
					} else {
						mSensorNameId = R.string.SensorName1_Extend;
					}
					break;
				case (int) PalmSecureConstant.JAVA_PvAPI_INFO_SENSOR_TYPE_9:
					mSensorNameId = R.string.SensorName8;
					mUsingSensorType = PalmSecureConstant.JAVA_PvAPI_INFO_SENSOR_TYPE_2;
					break;
				case (int) PalmSecureConstant.JAVA_PvAPI_INFO_SENSOR_TYPE_B:
					mSensorNameId = R.string.SensorNameA;
					isSupportedSensor = false;
					break;
				case (int) PalmSecureConstant.JAVA_PvAPI_INFO_SENSOR_TYPE_D:
					mSensorNameId = R.string.SensorNameC;
					isIdentifyEnable = false;
					break;
			}
			String dataTypeMsg = null;
			if ( mUsingDataType == PS_GEXTENDED_DATA_TYPE[0] ) {
				dataTypeMsg = mActivity.getResources().getString(R.string.DataMode1);
			} else if ( mUsingDataType == PS_GEXTENDED_DATA_TYPE[1] ) {
				dataTypeMsg = mActivity.getResources().getString(R.string.DataMode2);
			} else {
				dataTypeMsg = mActivity.getResources().getString(R.string.DataMode0);
			}

			String guideMsg = null;
			if ( mUsingGuideMode == 0 ) {
				guideMsg = mActivity.getResources().getString(R.string.GuideMode0);
			} else {
				guideMsg = mActivity.getResources().getString(R.string.GuideMode1);
			}
//			mTitle.setText(String.format("%s %s %s", mActivity.getResources().getString(mSensorNameId),
//					dataTypeMsg, guideMsg));
			Ps_Sample_Apl_Java_InitIdList(true);
			Ps_Sample_Apl_Java_SetComponentEnabled(BTN_STATE_WAITING);
		} else {
			Ps_Sample_Apl_Java_ShowErrorDialog(stResult);
		}
	}

	@Override
	public void Ps_Sample_Apl_Java_NotifyService_Connected() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_NotifyService_Connected");

		mActivity.registerReceiver(mUsbReceiver, new IntentFilter(ACTION_USB_PERMISSION));

		Ps_Sample_Apl_Java_CheckSensor();
	}

	public void Ps_Sample_Apl_Java_CheckSensor() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_RequestPermission");

		boolean hasPermission = false;
		boolean requestPermission = false;
		UsbManager usbMng = (UsbManager) mActivity.getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> deviceList = usbMng.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		PendingIntent pendIntent = PendingIntent.getBroadcast(mActivity, 0, new Intent(ACTION_USB_PERMISSION), 0);

		if (mChkDeviceList == null) {
			mChkDeviceList = new HashMap<String, UsbDevice>();
		}

		int vid=0, pid=0;
		while (deviceIterator.hasNext()) {
			UsbDevice device = deviceIterator.next();

			if (BuildConfig.DEBUG) {
				Log.d(TAG, device.toString());
			}

			vid = device.getVendorId();
			pid = device.getProductId();

			if (vid == USB_DEVICE_VENDOR_ID
					&& (pid == PRODUCT_ID_PALMSECURE_F_PRO)) {
				if (usbMng.hasPermission(device)) {
					if (BuildConfig.DEBUG) {
						Log.d(TAG, "hasPermission");
					}
					hasPermission = true;
				} else {
					if (!mChkDeviceList.containsValue(device)) {
						mChkDeviceList.put(device.getDeviceName(), device);
						usbMng.requestPermission(device, pendIntent);
						requestPermission = true;
						if (BuildConfig.DEBUG) {
							Log.d(TAG, "requestPermission");
						}
						break;
					}
				}
			}
		}

		if (requestPermission == false) {
			if (hasPermission == true) {
				Ps_Sample_Apl_Java_InitLibrary();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
				builder.setCancelable(false);
				builder.setTitle("Caution").setMessage("The sensor is not connected.");
				builder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
				builder.create().show();
			}
		}
	}

	@Override
	public void Ps_Sample_Apl_Java_NotifyService_Disconnected() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_NotifyService_Disconnected");
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setCancelable(false);
		builder.setTitle("Caution").setMessage("Service stopped.");
		builder.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mActivity.finish();
					}
				});
		builder.create().show();
	}

	@Override
	public int describeContents() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "describeContents");
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "writeToParcel");
	}

	private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "mUsbReceiver : " + action);
			}

			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (mActivity) {
					UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (BuildConfig.DEBUG) {
							Log.d(TAG, "permission granted for device " + device);
						}
					} else {
						if (BuildConfig.DEBUG) {
							Log.d(TAG, "permission denied for device " + device);
						}
					}

					Ps_Sample_Apl_Java_CheckSensor();
				}
			}
		}
	};

	private void Ps_Sample_Apl_Java_ShowErrorDialog(PsThreadResult stResult) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Ps_Sample_Apl_Java_ShowErrorDialog");

		boolean finishFlag = false;
		String msg = null;

		if (stResult.pseErrNumber != 0) {
			finishFlag = true;
			msg = PsMessageDialog.Ps_Sample_Apl_Java_ShowErrorDialog(stResult.pseErrNumber);
		} else if (stResult.messageKey != 0) {
			if (stResult.messageKey == R.string.AplErrorSystemError) {
				finishFlag = true;
			}
			msg = mActivity.getResources().getString(stResult.messageKey);
		} else if (stResult.errInfo.ErrorLevel != 0) {
			finishFlag = true;
			msg = PsMessageDialog.Ps_Sample_Apl_Java_ShowErrorDialog(mActivity, stResult.errInfo);
		}

		if (msg != null) {
			DialogInterface.OnClickListener listener;

			if (finishFlag == true) {
				listener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Ps_Sample_Apl_Java_TermLibrary();
						mActivity.finish();
					}
				};
			} else {
				listener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				};
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setCancelable(false);
			builder.setTitle("Caution").setMessage(msg);
			builder.setPositiveButton("OK", listener);
			builder.create().show();
		}
	}
}
