/*
 *	PsBusinessListener.java
 *
 *	All Rights Reserved, Copyright(c) FUJITSU FRONTECH LIMITED 2021
 */

package com.fujitsu.frontech.palmsecure_sample.event;

import java.util.ArrayList;

import com.fujitsu.frontech.palmsecure.JAVA_BioAPI_GUI_BITMAP;
import com.fujitsu.frontech.palmsecure_sample.data.PsThreadResult;

public interface PsBusinessListener {

	void Ps_Sample_Apl_Java_NotifyWorkMessage(int messageId);

	void Ps_Sample_Apl_Java_NotifyWorkMessage(int messageId, int count);

	void Ps_Sample_Apl_Java_NotifyWorkMessage(int messageId, int count, int number);

	void Ps_Sample_Apl_Java_NotifyGuidance(int guidanceId, boolean error);

	void Ps_Sample_Apl_Java_NotifySilhouette(JAVA_BioAPI_GUI_BITMAP bitmap);

	void Ps_Sample_Apl_Java_NotifyResult_InitLibrary(PsThreadResult stResult, long type, long extKind);

	void Ps_Sample_Apl_Java_NotifyResult_Enroll(PsThreadResult stResult, int enrollscore);

	void Ps_Sample_Apl_Java_NotifyResult_Verify(PsThreadResult stResult);

	void Ps_Sample_Apl_Java_NotifyResult_Identify(PsThreadResult stResult);

	void Ps_Sample_Apl_Java_NotifyResult_Cancel(PsThreadResult stResult);

	void Ps_Sample_Apl_Java_NotifyService_Connected();

	void Ps_Sample_Apl_Java_NotifyService_Disconnected();

}
