package com.pretzlav.instapaper.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;

public class InstaActivityHelper {
	public static final int DIALOG = 42;
	
	private Activity mActivity;
	private String mDialogMessage = "Loading...";
	
	public InstaActivityHelper(Activity activity) {
		mActivity = activity;
	}
	
	public void setDialogMessage(String message) {
		mDialogMessage= message;
	}
	
	public Dialog onCreateDialog(int id) {
		if (id == DIALOG) {
			ProgressDialog dialog = new ProgressDialog(mActivity);
			dialog.setMessage(mDialogMessage);
			return dialog;
		}
		return null;
	}
}
