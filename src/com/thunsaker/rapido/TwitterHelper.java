package com.thunsaker.rapido;

import java.io.IOException;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.SignatureType;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;

public class TwitterHelper {
	private static final String TAG = "TwitterHelper";
	
	private static Boolean sendTweet(Context myContext, String msg) throws Exception {		
		if(PreferencesHelper.getTwitterEnabled(myContext)) {
			String token = PreferencesHelper.getTwitterToken(myContext) != null ? PreferencesHelper.getTwitterToken(myContext) : "";
			String secret = PreferencesHelper.getTwitterSecret(myContext) != null ? PreferencesHelper.getTwitterSecret(myContext) : "";
			
			if(token != "" && secret != "") {
				AccessToken at = new AccessToken(token, secret);
				Twitter twitter = new TwitterFactory().getInstance();
				twitter.setOAuthConsumer(TwitterAuthorizationActivity.TWIT_KEY, TwitterAuthorizationActivity.TWIT_SECRET);
				twitter.setOAuthAccessToken(at);
				twitter.updateStatus(msg);
				return true;
			}
		}
		return false;
	}
	
	public static class SendTweet extends AsyncTask<Void, Integer, Boolean> {
		Context myContext;
		String myMessage;
		public SendTweet(Context theContext, String msg) {
			myContext = theContext;
			myMessage = msg;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				return sendTweet(myContext, myMessage);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
			if (!result) {
				// Toast.makeText(myContext, "Twitter message not sent, try again", Toast.LENGTH_SHORT).show();
				// Display notification that will allow reposting message...
				// PreferencesHelper.setTwitterFailedMessage(myContext, myMessage);
			}
		}
	}
	
	public static class TokenFetcher extends AsyncTask<Void, Integer, Boolean> {
		Context myContext;
		OAuthGetAccessToken myOauthGetAccessToken;
		public TokenFetcher(Context theContext, 
				OAuthGetAccessToken theGetAccessToken) {
			myContext = theContext;
//			Log.i(TAG, String.format("theGetAccessToken.temporaryToken :%s", theGetAccessToken.temporaryToken));
			myOauthGetAccessToken = theGetAccessToken;
//			Log.i(TAG, String.format("myOauthGetAccessToken.verifier :%s", myOauthGetAccessToken.verifier));
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
//				Log.i(TAG, String.format("theGetAccessToken.buildRelativeUrl() in doInBackground: %s", myOauthGetAccessToken.buildRelativeUrl()));
				OAuthCredentialsResponse myResponse = myOauthGetAccessToken.execute();
				
				if(myResponse != null && myResponse.token != null && myResponse.tokenSecret != null) {					
					PreferencesHelper.setTwitterToken(myContext, myResponse.token);
					PreferencesHelper.setTwitterSecret(myContext, myResponse.tokenSecret);
					
					PreferencesHelper.setTwitterEnabled(myContext, true);
					PreferencesHelper.setTwitterConnected(myContext, true);
					return true;
				}
			} catch (IOException e) {
				Log.i(TAG, "IOException");
				e.printStackTrace();
			} catch (Exception e) {
				Log.i(TAG, "Exception");
				e.printStackTrace();
			}
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			String resultMessage = myContext.getString(R.string.twitter_auth_failed);
			
			if(result)
				resultMessage = myContext.getString(R.string.twitter_auth_connected);
				
			Toast.makeText(myContext, resultMessage, Toast.LENGTH_SHORT).show();
		}
	}
}