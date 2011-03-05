package com.pretzlav.instapaper.api;

import android.text.TextUtils;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HTTP;

import com.pretzlav.android.net.http.AndroidHttpClient;
import com.pretzlav.instapaper.R;
import com.pretzlav.instapaper.application.InstaApper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class ApiRequest {

	private String mUrl;
	private List<? extends NameValuePair> mParams;
	private CommonsHttpOAuthConsumer mConsumer;
	private HttpClient mClient;
	private HttpResponse mResponse;
	
	
	public ApiRequest(String url, List<? extends NameValuePair> params, 
			String consumerKey, String consumerSecret, HttpClient httpClient) {
		mUrl = url;
		mParams = params;
		mConsumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
		mClient = httpClient;
	}
	
	public ApiRequest(String url, List<? extends NameValuePair> params, InstaApper application) {
		this(url, params, application.getString(R.string.oauth_consumer_key),
				application.getString(R.string.oauth_consumer_secret),
				application.getHttpClient());
		if (!TextUtils.isEmpty(application.getOAuthToken()) && 
				!TextUtils.isEmpty(application.getOAuthTokenSecret())) {
			mConsumer.setTokenWithSecret(application.getOAuthToken(), 
					application.getOAuthTokenSecret());
		}
	}
	
	public OAuthConsumer getConsumer() {
		return mConsumer;
	}
	
	public HttpResponse getResponse() {
		return mResponse;
	}
	
	public InputStream execute() {
		HttpPost request = new HttpPost(mUrl);
		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(mParams, HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("wtf");
		}
		request.setEntity(entity);
		AndroidHttpClient.modifyRequestToAcceptGzipResponse(request);
		try {
			mConsumer.sign(request);
		} catch (OAuthMessageSignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InputStream data = null;
		try {
			mResponse = mClient.execute(request);
			data = AndroidHttpClient.getUngzippedContent(mResponse.getEntity());
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
	
	public String executeAndRead() {
		InputStream data = execute();
		if (data != null) {
			return readResponseToString(data);
		} else {
			return null;
		}
	}
	
	public static String readResponseToString(InputStream response) {
		String responseString = null;
		try {
			final char[] buffer = new char[0x10000];
			StringBuilder out = new StringBuilder();
			Reader in = new InputStreamReader(response, HTTP.UTF_8);
			int read;
			do {
				read = in.read(buffer, 0, buffer.length);
				if (read > 0) {
					out.append(buffer, 0, read);
				}
			} while (read >= 0);
			in.close();
			responseString = out.toString();
		} catch (IOException ioe) {
			throw new IllegalStateException("Error while reading response body", ioe);
		}
		return responseString;
	}
	
	public static void readResponseToFile(InputStream response, File target) throws FileNotFoundException {
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(target));
		try {
			final byte[] buffer = new byte[0x10000];
			int read;
			do {
				read = response.read(buffer, 0, buffer.length);
				if (read > 0) {
					out.write(buffer, 0, read);
				}
			} while (read >= 0);
			response.close();
			out.close();
		} catch (IOException ioe) {
			throw new IllegalStateException("Error while reading response body", ioe);
		}
	}
}
