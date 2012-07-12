package com.thunsaker.rapido.android;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.content.Context;
import android.widget.Toast;

public class TwitterHelper {
	public static Boolean sendTweet(Context myContext, String msg) throws Exception {
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
}
