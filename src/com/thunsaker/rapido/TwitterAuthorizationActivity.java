package com.thunsaker.rapido;

import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.google.api.client.auth.oauth.OAuthGetTemporaryToken;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.http.apache.ApacheHttpTransport;

public class TwitterAuthorizationActivity extends SherlockActivity {
	final String TAG = getClass().getName();
	
	private boolean useLogo = true;
    private boolean showHomeUp = true;
    
	public final static String TWIT_KEY = "YOUR_TWITTER_KEY";
	public final static String TWIT_SECRET = "YOUR_TWITTER_SECRET";
	public final static String OAUTH_CALLBACK_URL = "YOUR_CALLBACK_URL";
	
	public static final String REQUEST_URL = "http://api.twitter.com/oauth/request_token";
	public static final String ACCESS_URL = "http://api.twitter.com/oauth/access_token";
	public static final String AUTHORIZE_URL = "http://api.twitter.com/oauth/authorize";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        final com.actionbarsherlock.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(showHomeUp);
        ab.setDisplayUseLogoEnabled(useLogo);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	        	finish();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		WebView webView = new WebView(this);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setVisibility(View.VISIBLE);
		setContentView(webView);
		
		final OAuthHmacSigner signer = new OAuthHmacSigner();
		signer.clientSharedSecret = TWIT_SECRET;
		
		OAuthGetTemporaryToken tempToken = new OAuthGetTemporaryToken(REQUEST_URL);
		tempToken.transport = new ApacheHttpTransport();
		tempToken.signer = signer;
		tempToken.consumerKey = TWIT_KEY;
		tempToken.callback = OAUTH_CALLBACK_URL;
		
		try {
			OAuthCredentialsResponse tempCredentials = tempToken.execute();
			signer.tokenSharedSecret = tempCredentials.tokenSecret;
			
			OAuthAuthorizeTemporaryTokenUrl authorizeUrl = new OAuthAuthorizeTemporaryTokenUrl(AUTHORIZE_URL);
			authorizeUrl.temporaryToken = tempCredentials.token;
			String authorizationUrl = authorizeUrl.build();
			webView.setWebViewClient(new WebViewClient(){
				@Override
				public void onPageStarted(WebView view, String url,
						Bitmap favicon) {
					Log.i(TAG, "Url started: " + url);
				}
				
				@Override
				public void onPageFinished(WebView view, String url) {
					Log.i(TAG, "On Page Finished: " + url);
					if(url.startsWith(OAUTH_CALLBACK_URL)){
						Log.i(TAG, "After Url Match: " + url);
						try {
							if(url.indexOf("oauth_token=") != -1) {
								PreferencesHelper.setTwitterToken(getApplicationContext(), "");
								PreferencesHelper.setTwitterSecret(getApplicationContext(), "");
								
								String requestToken = extractParamFromUrl(url, "oauth_token");
								String verifier = extractParamFromUrl(url, "oauth_verifier");
								Log.i(TAG, "My Tokens: " + requestToken + ", " + verifier);
								signer.clientSharedSecret = TWIT_SECRET;
								
								OAuthGetAccessToken accessToken = new OAuthGetAccessToken(ACCESS_URL);
								accessToken.transport = new ApacheHttpTransport();
								accessToken.temporaryToken = requestToken;
								accessToken.signer = signer;
								accessToken.consumerKey = TWIT_KEY;
								accessToken.verifier = verifier;
							
								OAuthCredentialsResponse credentials = accessToken.execute();
								PreferencesHelper.setTwitterToken(getApplicationContext(), credentials.token);
								PreferencesHelper.setTwitterSecret(getApplicationContext(), credentials.tokenSecret);
								PreferencesHelper.setTwitterEnabled(getApplicationContext(), true);
								PreferencesHelper.setTwitterConnected(getApplicationContext(), true);
								view.setVisibility(View.INVISIBLE);
								startActivity(new Intent(getApplicationContext(), MainActivity.class));
							} else if (url.indexOf("error=") != -1) {
								view.setVisibility(View.INVISIBLE);
								Log.i(TAG, "No match: " + url);
								//startActivity(new Intent(getApplicationContext(), MainActivity.class));
								finish();
							}
						} catch (IOException e) {
							Log.i(TAG, "IOException: " + e.getMessage());
							e.printStackTrace();
						}
					}
				}
			});
			
			webView.loadUrl(authorizationUrl);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}
	}
	
	private String extractParamFromUrl(String url,String paramName) {
		String queryString = url.substring(url.indexOf("?", 0)+1,url.length());
		QueryStringParser queryStringParser = new QueryStringParser(queryString);
		return queryStringParser.getQueryParamValue(paramName);
	}  
}
