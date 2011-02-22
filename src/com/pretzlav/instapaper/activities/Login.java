package com.pretzlav.instapaper.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.message.BasicNameValuePair;

import com.pretzlav.instapaper.R;
import com.pretzlav.instapaper.api.ApiRequest;
import com.pretzlav.instapaper.application.InstaApper;

import java.util.Arrays;
import java.util.List;

public class Login extends Activity {
	
	private LoginTask mLoginTask;
	private InstaActivityHelper mHelper;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.login);
		mHelper = new InstaActivityHelper(this);
		mHelper.setDialogMessage("Logging in...");
		mLoginTask = (LoginTask)getLastNonConfigurationInstance();
		if (mLoginTask != null) {
			mLoginTask.setActivity(this);
		}
		InstaApper mApp = (InstaApper)getApplication();
		if (!TextUtils.isEmpty(mApp.getOAuthToken()) && 
				!TextUtils.isEmpty(mApp.getOAuthTokenSecret())) {
			Intent intent = new Intent(this, ArticleList.class);
			startActivity(intent);
			finish();
		}
	}
	
	public void onLoginPressed(View v) {
		String login = ((TextView)findViewById(R.id.login)).getText().toString();
		String password = ((TextView)findViewById(R.id.password)).getText().toString();
		showDialog(InstaActivityHelper.DIALOG);
		login(login, password);
	}
	
	void login(String login, String password) {
		if (mLoginTask != null) {
			mLoginTask.setActivity(null);
		}
		mLoginTask = new LoginTask(this);
		mLoginTask.execute(login, password);
	}
	
	@Override
	public LoginTask onRetainNonConfigurationInstance() {
		return mLoginTask;
	}
	
	@Override
	public Dialog onCreateDialog(int id) {
		return mHelper.onCreateDialog(id);
	}
	
	void onLoginSuccess(String token, String tokenSecret) {
		InstaApper mApp = (InstaApper)getApplication();
		mApp.saveOAuthToken(token);
		mApp.saveOAuthTokenSecret(tokenSecret);
		Intent intent = new Intent(this, ArticleList.class);
		startActivity(intent);
		finish();
	}
	
	void onLoginFailure(ApiRequest request) {
		Toast.makeText(this, "There was a problem logging in. Please try again.", 
				Toast.LENGTH_LONG).show();
	}
	
	private static class LoginTask extends AsyncTask<String, Void, String[]> {

		Login mActivity;
		private ApiRequest mRequest;
		
		public LoginTask(Login activity) {
			mActivity = activity;
		}
		
		void setActivity(Login activity) {
			mActivity = activity;
		}
		
		@Override
		protected String[] doInBackground(String... credentials) {
			List<BasicNameValuePair> params = Arrays.asList(
					new BasicNameValuePair("x_auth_username", credentials[0]),
					new BasicNameValuePair("x_auth_password", credentials[1]),
					new BasicNameValuePair("x_auth_mode", "client_auth"));
			 mRequest = new ApiRequest("https://www.instapaper.com/api/1/oauth/access_token",
						params, (InstaApper)mActivity.getApplication());
			String responseString = mRequest.execute();
		    
		    return TextUtils.split(responseString, "&");
		}
		
		@Override
		public void onPostExecute(String[] response) {
			if (response != null && 
					mRequest.getResponse().getStatusLine().getStatusCode() == 200) {
				String token = null;
				String tokenSecret = null;
				for (String str : response) {
					String[] pair = TextUtils.split(str, "=");
					if (pair.length < 2) {
						continue;
					}
					if ("oauth_token".equals(pair[0])) {
						token = pair[1];
					} else if ("oauth_token_secret".equals(pair[0])) {
						tokenSecret = pair[1];
					}
				}
				if (token != null && tokenSecret != null) {
					mActivity.onLoginSuccess(token, tokenSecret);
					return;
				}
			}
			mActivity.onLoginFailure(mRequest);
		}
	}
	
}
