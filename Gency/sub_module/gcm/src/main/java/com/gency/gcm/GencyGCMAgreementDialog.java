package com.gency.gcm;

import android.content.Context;
import android.content.SharedPreferences.Editor;

import com.gency.commons.dialog.GencyBaseAgreementDialog;

public class GencyGCMAgreementDialog extends GencyBaseAgreementDialog {
	public GencyGCMAgreementDialog(Context context, int eulaVersion, String eulaUrl, String prefKey) {
		super(context, eulaVersion, eulaUrl, prefKey, null, null, null);
	}

	@Override
	protected void handleDecline() {
		super.handleDecline();
		Editor e = mPref.edit();
		e.putBoolean(GencyGCMConst.PREF_KEY_WILLSENDNOTIFICATION, false);
		e.commit();
		super.saveAgreement();
	}

	@Override
	protected void handleAgree() {
		super.handleAgree();
		Editor e = mPref.edit();
		e.putBoolean(GencyGCMConst.PREF_KEY_WILLSENDNOTIFICATION, true);
		e.putBoolean(GencyGCMConst.PREF_KEY_WILLPLAYSOUND, true);
		e.putBoolean(GencyGCMConst.PREF_KEY_WILLVIBRATE, true);
		e.commit();
	}
}