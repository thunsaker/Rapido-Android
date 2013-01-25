package com.thunsaker.rapido;

import java.util.Calendar;
import java.util.List;

import com.thunsaker.rapido.classes.Draft;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class FacebookHelper {	
	private static String updateStatus(Context theContext, FacebookAuthenticationHelper fbAuth, String update) throws Exception {
		String response = fbAuth.facebook.request("me");
		Bundle parameters = new Bundle();
		
		// Remote Hashtags Preference
		if(PreferencesHelper.getFacebookDeleteHashtags(theContext)) {
			List<String> hashtagList = Util.GetHashtagsInText(update);
			if(hashtagList != null && hashtagList.size() > 0) {
				for (String hashtag : hashtagList) {
					int tagStart = update.indexOf("#" + hashtag);
					update = update.substring(0, tagStart - 1) + update.substring(tagStart + hashtag.length() + 1, update.length()); 
				}
			}
		}

		parameters.putString("message", update);
		parameters.putString("description", "Posting from Rapido for Android");
		
//		// TODO: Implement post to facebook as link instead of text 
//		List<String> linksInText = Util.GetLinksInText(update);
//		// TODO: Turn this into a setting
//		if(linksInText != null) {
//			parameters.putString("link", linksInText.get(0));
//		}
		
		response = fbAuth.facebook.request("me/feed", parameters, "POST");
		
		return response;
	}
	
	public static class UpdateStatus extends AsyncTask<Void, Integer, String> {
		FacebookAuthenticationHelper myFbAuth;
		String myMessage;
		Context myContext;
		
		public UpdateStatus(Context theContext, FacebookAuthenticationHelper fbAuth, String message) {
			myFbAuth = fbAuth;
			myMessage = message;
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
			NotificationManager mNotificationManager = 
					(NotificationManager) myContext.getSystemService(Context.NOTIFICATION_SERVICE);
			
			if (result == null || result.equals("") || result.equals("false")) {
				// Display notification that will allow reposting message...
				Intent facebookRepost = new Intent(myContext, MainActivity.class);
				facebookRepost.putExtra(Intent.EXTRA_TEXT, String.format("%s %s", MainActivity.REPOST_FACEBOOK, myMessage));
				TaskStackBuilder stackBuilder = TaskStackBuilder.create(myContext);
				stackBuilder.addParentStack(MainActivity.class);
				stackBuilder.addNextIntent(facebookRepost);
				PendingIntent repostPendingIntent = 
				        stackBuilder.getPendingIntent(
				            0,
				            PendingIntent.FLAG_UPDATE_CURRENT
				        );
				
				Calendar myCalendar = Calendar.getInstance();
				Integer currentTimeInSeconds = myCalendar.get(Calendar.SECOND);
				
				Draft myDraft = new Draft(myMessage);
				myDraft.setKey(MainActivity.REPOST_FACEBOOK);
				myDraft.setDateSaved(currentTimeInSeconds.toString());
				myDraft.setTwitterPosted(true);
				myDraft.setFacebookPosted(false);
				myDraft.setFailedToPost(true);
				
				MainActivity.saveDraft(MainActivity.REPOST_FACEBOOK, myDraft, myContext);
				mNotificationManager.cancel(MainActivity.FACEBOOK_NOTIFICATION);
				
				NotificationCompat.Builder mNotificationFacebookFail = 
						new NotificationCompat.Builder(myContext)
							.setSmallIcon(R.drawable.ic_stat_rapido)
							.setContentTitle(myContext.getString(R.string.alert_fail_facebook))
							.setContentText(myContext.getString(R.string.alert_fail_retry))
							.setAutoCancel(true)
							.setContentIntent(repostPendingIntent)
							.setDefaults(Notification.DEFAULT_ALL);
				mNotificationManager.notify(MainActivity.FACEBOOK_NOTIFICATION, mNotificationFacebookFail.getNotification());
				
				//PreferencesHelper.setFacebookFailedMessage(myContext, myMessage);
			} else {
				Log.i("FacebookHelper", result);
				
				NotificationCompat.Builder mNotificationFacebookSuccess = 
						new NotificationCompat.Builder(myContext)
							.setSmallIcon(R.drawable.ic_stat_rapido)
							.setContentTitle(myContext.getString(R.string.alert_title))
							.setContentText(myContext.getString(R.string.alert_posted_facebook))
							.setAutoCancel(true)
							.setContentIntent(MainActivity.genericPendingIntent);
				mNotificationManager.notify(MainActivity.FACEBOOK_NOTIFICATION, mNotificationFacebookSuccess.getNotification());
				mNotificationManager.cancel(MainActivity.FACEBOOK_NOTIFICATION);
			}
		}
	}
}