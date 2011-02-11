package com.pretzlav.instapaper.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;

import com.pretzlav.instapaper.R;
import com.pretzlav.instapaper.api.InstapaperApi;

public class Login extends Activity {

	public static final int DIALOG_LOGGING_IN = 42;
	
	private LoginTask mLoginTask;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.login);
		mLoginTask = (LoginTask)getLastNonConfigurationInstance();
		if (mLoginTask != null) {
			mLoginTask.setActivity(this);
		}
	}
	
	public void onLoginPressed(View v) {
		String login = ((TextView)findViewById(R.id.login)).getText().toString();
		String password = ((TextView)findViewById(R.id.password)).getText().toString();
		showDialog(DIALOG_LOGGING_IN);
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
		if (id == DIALOG_LOGGING_IN) {
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage("Logging in...");
			return dialog;
		}
		return null;
	}
	
	private static class LoginTask extends AsyncTask<String, Void, String[]> {

		Login mActivity;
		
		public LoginTask(Login activity) {
			mActivity = activity;
		}
		
		void setActivity(Login activity) {
			mActivity = activity;
		}
		
		@Override
		protected String[] doInBackground(String... credentials) {
			// For now, use OAuthRequest directly, as we don't have correct tokens
//			OAuthService service = new ServiceBuilder()
//					.provider(InstapaperApi.class)
//					.apiKey(mActivity.getString(R.string.oauth_consumer_key))
//					.apiSecret(mActivity.getString(R.string.oauth_consumer_secret))
//					.build();
			
			InstapaperApi api = new InstapaperApi();
			
			String consumer_secret = mActivity.getString(R.string.oauth_consumer_secret);
			OAuthRequest request = new OAuthRequest(Verb.POST,"http://www.instapaper.com/api/1/oauth/access_token");
			request.addOAuthParameter(OAuthConstants.CONSUMER_KEY, mActivity.getString(R.string.oauth_consumer_key));
			request.addOAuthParameter(OAuthConstants.CONSUMER_SECRET, consumer_secret);
		    request.addOAuthParameter(OAuthConstants.TIMESTAMP, api.getTimestampService().getTimestampInSeconds());
		    request.addOAuthParameter(OAuthConstants.NONCE, api.getTimestampService().getNonce());
		    request.addOAuthParameter(OAuthConstants.SIGN_METHOD, api.getSignatureService().getSignatureMethod());
		    request.addOAuthParameter(OAuthConstants.VERSION, "1.0");
		    String baseString = api.getBaseStringExtractor().extract(request);
		    String signature = api.getSignatureService().getSignature(baseString, consumer_secret, "");
		    request.addOAuthParameter(OAuthConstants.SIGNATURE, signature);
		    
		    request.addBodyParameter("x_auth_username", credentials[0]);
		    request.addBodyParameter("x_auth_password", credentials[1]);
		    request.addBodyParameter("x_auth_mode", "client_auth");
		    
		    String oauthHeader = api.getHeaderExtractor().extract(request);
		    request.addHeader(OAuthConstants.HEADER, oauthHeader);
		    Response response = request.send();
		    Token token = api.getAccessTokenExtractor().extract(response.getBody());
		    return new String[] {token.getToken(), token.getSecret()};
		}
		
	}
	
}
