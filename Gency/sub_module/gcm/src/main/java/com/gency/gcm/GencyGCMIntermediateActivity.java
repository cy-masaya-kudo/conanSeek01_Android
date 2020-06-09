package com.gency.gcm;

import android.app.Activity;
import android.os.Bundle;

import com.gency.gcm.GencyGCMTransfer;

public class GencyGCMIntermediateActivity extends Activity{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GencyGCMTransfer.action(this);
	}

}
