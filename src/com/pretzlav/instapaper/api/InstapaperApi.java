package com.pretzlav.instapaper.api;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;

public class InstapaperApi extends DefaultApi10a {

	@Override
	public String getAccessTokenEndpoint() {
		return "https://www.instapaper.com/api/1/oauth/access_token";
	}

	@Override
	public String getAuthorizationUrl(Token arg0) {
		return null;
	}

	/**
	 * NOTE: This url does not exist. DON'T USE IT.
	 * Instapaper doesn't need it.
	 */
	@Override
	public String getRequestTokenEndpoint() {
		return "https://www.instapaper.com/api/1/oauth/request_token";
	}

}
