package com.pretzlav.instapaper.application;

import android.app.Application;
import android.content.Context;

import org.apache.http.client.HttpClient;

import com.pretzlav.android.net.http.AndroidHttpClient;

import java.io.File;

public class InstaApper extends Application {

	private static final String USERAGENT = "alexs-client-for-instapaper";
	
	public static final String PREF_NAME = "alexs-instapaper";
	
	private static final String KEY_TOKEN = "token";
	private static final String KEY_SECRET = "secret";
	
	private HttpClient mHttpClient;
	
	private String mOAuthToken;
	private String mOAuthTokenSecret;
	
	public synchronized HttpClient getHttpClient() {
		if (mHttpClient == null) {
			mHttpClient = AndroidHttpClient.newInstance(USERAGENT);
		}
		return mHttpClient;
	}
	
	public static HttpClient getHttpClient(Context context) {
		return ((InstaApper)context.getApplicationContext()).getHttpClient();
	}
	
	public String getOAuthToken() {
		if (mOAuthToken == null) {
			mOAuthToken = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString(KEY_TOKEN, "");
		}
		return mOAuthToken;
	}
	
	public void saveOAuthToken(String token) {
		getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().putString(KEY_TOKEN, token).commit();
		mOAuthToken = token;
	}
	
	public String getOAuthTokenSecret() {
		if (mOAuthTokenSecret == null) {
			mOAuthTokenSecret = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString(KEY_SECRET, "");
		}
		return mOAuthTokenSecret;
	}
	
	public void saveOAuthTokenSecret(String secret) {
		getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().putString(KEY_SECRET, secret).commit();
		mOAuthTokenSecret = secret;
	}
	
	public File getPageCacheDir() {
		File cacheDir = new File(getCacheDir(), "articles");
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		return cacheDir;
	}
}
