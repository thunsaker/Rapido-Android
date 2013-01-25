package com.thunsaker.rapido;

import java.util.List;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thunsaker.rapido.classes.ShortUrl;

public class BitlyHelper {
	public final static String LOG_TAG = "BitlyHelper";

	private static String BITLY_BASE_URL = "https://api-ssl.bitly.com/v3/";
	
	public static class ShortenAndSend extends AsyncTask<Void, Integer, Boolean> {
		Context myContext;
		String myUpdate;
		FacebookAuthenticationHelper myFbAuth;
		Integer myService;
		
		public ShortenAndSend(Context theContext, Integer theService, String theUpdate, Object theFbAuth) {
			myContext = theContext;
			myUpdate = theUpdate;
			myService = theService;
			if(myService == Util.FACEBOOK_UPDATE)
				myFbAuth = (FacebookAuthenticationHelper)theFbAuth;
			else
				myFbAuth = null;
			
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				List<String> urlsInUpdate = Util.GetLinksInText(myUpdate);
				if(!urlsInUpdate.isEmpty()) {
					String originalUrl;
					String shortenedUrl;
					for (String theUrl : urlsInUpdate) {
						originalUrl = theUrl;
						ShortUrl myShortUrl = Util.ShortenUrl(theUrl, myContext);
						if(myShortUrl != null) {
							shortenedUrl = myShortUrl.getUrl();
							if(shortenedUrl != null && shortenedUrl != "") {
								myUpdate = myUpdate.replace(originalUrl, shortenedUrl);
							} else {
								return false;
							}
						} else {
							return false;
						}
					}
				}
				
				return true;
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
			
			NotificationCompat.Builder mNotificationBitly = 
					new NotificationCompat.Builder(myContext)
						.setSmallIcon(R.drawable.ic_stat_rapido)
						.setContentTitle(myContext.getString(R.string.alert_title))
						.setContentText(myContext.getString(R.string.alert_posting_shortened_bitly))
						.setContentIntent(MainActivity.genericPendingIntent);;
			
			mNotificationManager.cancel(MainActivity.BITLY_NOTIFICATION);
			if(myService == Util.FACEBOOK_UPDATE) {
				new FacebookHelper.UpdateStatus(myContext, myFbAuth, myUpdate).execute();
			} else if(myService == Util.TWITTER_UPDATE) {
				new TwitterHelper.SendTweet(myContext, myUpdate).execute();
			}
		}
	}
	
	public static class ShortenUrl extends AsyncTask<Void, Integer, String> {
		private String myBitlyAccessToken;
		private String myLongUrl;
		private ShortUrl myShortUrl;
		
		public ShortenUrl(Context myContext, String UrlToShorten) {
			myBitlyAccessToken = PreferencesHelper.getBitlyToken(myContext);
			myLongUrl = UrlToShorten;
		}

		@Override
		protected String doInBackground(Void... params) {
			myShortUrl = BitlyHelper.getShortUrl(myLongUrl, myBitlyAccessToken);
			if(myShortUrl != null)
				return myShortUrl.getUrl();
			return null;
		}
	}
	
	// Shorten the URL
	public static ShortUrl getShortUrl(String l, String accessToken) {
		try {
			// get data from request
			String shortenRequestUrl = String.format("%sshorten?access_token=%s&longUrl=%s&format=json",
					BITLY_BASE_URL,
					accessToken, l);
			String jsonShortUrlResponse = Util.getHttpResponse(
					shortenRequestUrl,
					"", "");

			try {
				if (jsonShortUrlResponse != null) {
					JsonParser jParser = new JsonParser();
					JsonObject jObject = (JsonObject) jParser.parse(jsonShortUrlResponse);

					if (jObject != null) {
						if (Integer.parseInt(jObject.get("status_code").toString()) == 200) {
							//&& jObject.get("status_txt").toString() == "OK"
							JsonObject jObjectShortUrl = jObject.getAsJsonObject("data");
							Gson gson = new Gson();
							ShortUrl myShortUrl = gson.fromJson(jObjectShortUrl, ShortUrl.class);
							return myShortUrl;
						} else {
							// Failed to shorten
							return null;
						}
					} else {
						// Failed to parse response
						return null;
					}
				}

				return null;
			} catch (Exception e) {
				Log.e("Background_process", e.getMessage());
				return null;
			}
		} catch (Exception ex) {
			Log.e(LOG_TAG, "GetShortUrl Exception: " + ex.getMessage());
			return null;
		}
	}
}