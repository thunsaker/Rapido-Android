package com.thunsaker.rapido;

import java.io.IOException;
import java.util.Calendar;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.thunsaker.rapido.classes.Draft;

public class TwitterHelper {
	private static final String TAG = "TwitterHelper";
	
	private static Boolean sendTweet(Context myContext, String msg) throws Exception {
		Log.i(TAG, "Inside sendTweet - Top of the method");
		if(PreferencesHelper.getTwitterConnected(myContext)) {
			Log.i(TAG, "Inside sendTweet - after Preference check");
			String token = PreferencesHelper.getTwitterToken(myContext) != null ? PreferencesHelper.getTwitterToken(myContext) : "";
			String secret = PreferencesHelper.getTwitterSecret(myContext) != null ? PreferencesHelper.getTwitterSecret(myContext) : "";
			Log.i(TAG, "Inside sendTweet");
			if(token != "" && secret != "") {
				AccessToken at = new AccessToken(token, secret);
				Twitter twitter = new TwitterFactory().getInstance();
				twitter.setOAuthConsumer(AuthHelper.TWIT_KEY, AuthHelper.TWIT_SECRET);
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
				Log.i(TAG, "Doing the Background work...");
				return sendTweet(myContext, myMessage);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			NotificationManager mNotificationManager = 
					(NotificationManager) myContext.getSystemService(Context.NOTIFICATION_SERVICE);
			
			if(result) {
				NotificationCompat.Builder mNotificationTwitterPosted = 
						new NotificationCompat.Builder(myContext)
							.setSmallIcon(R.drawable.ic_stat_rapido)
							.setContentTitle(myContext.getString(R.string.alert_title))
							.setContentText(myContext.getString(R.string.alert_posted_twitter))
							.setContentIntent(MainActivity.genericPendingIntent);
				mNotificationManager.notify(MainActivity.TWITTER_NOTIFICATION, mNotificationTwitterPosted.getNotification());
				mNotificationManager.cancel(MainActivity.TWITTER_NOTIFICATION);
			} else {
				Intent twitterRepost = new Intent(myContext, MainActivity.class);
				twitterRepost.putExtra(Intent.EXTRA_TEXT, String.format("%s %s", MainActivity.REPOST_TWITTER, myMessage));
				TaskStackBuilder stackBuilder = TaskStackBuilder.create(myContext);
				stackBuilder.addParentStack(MainActivity.class);
				stackBuilder.addNextIntent(twitterRepost);
				PendingIntent repostPendingIntent = 
				        stackBuilder.getPendingIntent(
				            0,
				            PendingIntent.FLAG_UPDATE_CURRENT
				        );
				
				Calendar myCalendar = Calendar.getInstance();
				Integer currentTimeInSeconds = myCalendar.get(Calendar.SECOND);
				
				Draft myDraft = new Draft(myMessage);
				myDraft.setKey(MainActivity.REPOST_TWITTER);
				myDraft.setDateSaved(currentTimeInSeconds.toString());
				myDraft.setTwitterPosted(false);
				myDraft.setFacebookPosted(true);
				myDraft.setFailedToPost(true);

				MainActivity.saveDraft(MainActivity.REPOST_TWITTER, myDraft, myContext);
				mNotificationManager.cancel(MainActivity.TWITTER_NOTIFICATION);
				Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				
				NotificationCompat.Builder mNotificationTwitterFail = 
						new NotificationCompat.Builder(myContext)
							.setSmallIcon(R.drawable.ic_stat_rapido)
							.setContentTitle(myContext.getString(R.string.alert_fail_twitter))
							.setContentText(myContext.getString(R.string.alert_fail_retry))
							.setAutoCancel(true)
							.setContentIntent(repostPendingIntent)
							.setSound(defaultSound)
							.setDefaults(Notification.DEFAULT_ALL);
				mNotificationManager.notify(MainActivity.TWITTER_NOTIFICATION, mNotificationTwitterFail.getNotification());
			}
			
			//if (!result) {
				// Toast.makeText(myContext, "Twitter message not sent, try again", Toast.LENGTH_SHORT).show();
				// Display notification that will allow reposting message...
				// PreferencesHelper.setTwitterFailedMessage(myContext, myMessage);
			//}
		}
	}
	
	public static class TokenFetcher extends AsyncTask<Void, Integer, Boolean> {
		Context myContext;
		OAuthGetAccessToken myOauthGetAccessToken;
		
		public TokenFetcher(Context theContext, 
				OAuthGetAccessToken theGetAccessToken) {
			myContext = theContext;
			myOauthGetAccessToken = theGetAccessToken;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			OAuthCredentialsResponse myResponse = null;
			String token = PreferencesHelper.getTwitterToken(myContext);
			
			if(token == null || token.equalsIgnoreCase("")) {
				try {
					myResponse = myOauthGetAccessToken.execute();
				} catch (IOException e) {
					Log.i(TAG, "IOException");
					e.printStackTrace();
				} catch (Exception e) {
					Log.i(TAG, "Exception");
					e.printStackTrace();
				}
				
				Log.i(TAG, "Response: " + myResponse);
				
				if(myResponse != null && myResponse.token != null && myResponse.tokenSecret != null) {
					PreferencesHelper.setTwitterToken(myContext, myResponse.token);
					PreferencesHelper.setTwitterSecret(myContext, myResponse.tokenSecret);
					PreferencesHelper.setTwitterEnabled(myContext, true);
					PreferencesHelper.setTwitterConnected(myContext, true);
				}
			}
			
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			String resultMessage = myContext.getString(R.string.twitter_auth_failed);
			
			if(PreferencesHelper.getTwitterConnected(myContext))
				resultMessage = myContext.getString(R.string.twitter_auth_connected);
				
			Toast.makeText(myContext, resultMessage, Toast.LENGTH_SHORT).show();
		}
	}
}