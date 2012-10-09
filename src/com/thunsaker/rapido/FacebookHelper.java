package com.thunsaker.rapido;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

public class FacebookHelper {	
	private static String updateStatus(Context theContext, FacebookAuthenticationHelper fbAuth, String update) throws Exception {
		String response = fbAuth.facebook.request("me");
		Bundle parameters = new Bundle();
		parameters.putString("message", update);
		parameters.putString("description", "Posting from Rapido for Android");
		response = fbAuth.facebook.request("me/feed", parameters, "POST");
		return response;
	}
	
	public static class UpdateStatus extends AsyncTask<Void, Integer, String> {
		FacebookAuthenticationHelper myFbAuth;
		String myMessage;
		Boolean postToTwitter;
		Context myContext;
		
		public UpdateStatus(Context theContext, FacebookAuthenticationHelper fbAuth, String message, Boolean twitter) {
			myFbAuth = fbAuth;
			myMessage = message;
			postToTwitter = twitter;
			myContext = theContext;
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				return updateStatus(myContext, myFbAuth, myMessage);
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
			if (result == null || result.equals("") || result.equals("false")) {
				// Toast.makeText(myContext, R.string.error_not_posted, Toast.LENGTH_SHORT).show();
				// Display notification that will allow reposting message...
				// PreferencesHelper.setFacebookFailedMessage(myContext, myMessage);
			} else {
				if (postToTwitter) {
					new TwitterHelper.SendTweet(myContext, myMessage);
				}
			}
		}
	}
}
