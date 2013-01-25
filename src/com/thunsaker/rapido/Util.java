package com.thunsaker.rapido;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.NotificationManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.thunsaker.rapido.classes.ShortUrl;
import com.twitter.Extractor;

public class Util {
	private static final String LOG_TAG = "Util";
	public static final int FACEBOOK_UPDATE = 0;
	public static final int TWITTER_UPDATE = 1;

	public static String contentType = "json/application";

	public static ShortUrl ShortenUrl(String urlToShorten, Context myContext) {
		try {
			if (Util.HasInternet(myContext)) {
				//String longUrl = URLEncoder.encode(urlToShorten);
				String longUrl = urlToShorten;
				ShortUrl theShortenedUrl = new ShortUrl();

				if (longUrl != "") {
					String token = PreferencesHelper.getBitlyToken(myContext);
					theShortenedUrl = BitlyHelper.getShortUrl(longUrl, token);

					if (theShortenedUrl != null && theShortenedUrl.getUrl() != "") {
						// Url shortened properly
						return theShortenedUrl;
					} else {
						// Error shorten
					}
				} else {
					// Error no url to shorten
				}
			} else {
				// Error
			}

			return null;
		} catch (Exception ex) {
			Log.e(LOG_TAG + ".shortenUrl",
					"shortenUrl() - Exception: " + ex.getMessage());
			return null;
		}
	}
	
	static String getHttpResponse(String url, String contentType, String accepts) {
		return getHttpResponse(url, false, contentType, accepts);
	}

	static String getHttpResponse(String url, Boolean isHttpPost, String contentType, String accepts) {
		Log.i(LOG_TAG, "getHttp: " + url);
		String result = "";
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		HttpPost httpPost = new HttpPost(url);
		HttpResponse response;

		try {
			if(isHttpPost)
				response = httpclient.execute(httpPost);
			else
				response = httpclient.execute(httpGet);

			Log.i(LOG_TAG, String.format("Get Response", response));
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				result = convertStreamToString(instream);
				instream.close();
			}
		} catch (ClientProtocolException e) {
			Log.e(LOG_TAG, "There was a protocol based error", e);
		} catch (IOException e) {
			Log.e(LOG_TAG, "There was an IO Stream related error", e);
		}
		return result;
	}
	
	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	@SuppressWarnings("static-access")
	public static Boolean HasInternet(Context myContext) {
		Boolean HasConnection = false;
		ConnectivityManager connectivityManager = (ConnectivityManager) myContext
				.getSystemService(myContext.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		if (activeNetworkInfo != null) {
			State myState = activeNetworkInfo.getState();
			if (myState == State.CONNECTED || myState == State.CONNECTING) {
				HasConnection = true;
			}
		}
		return HasConnection;
	}
	
	public static List<String> GetLinksInText(String textToCheck) {
		Extractor twitterExtractor = new Extractor();
		List<String> myLinks = twitterExtractor.extractURLs(textToCheck);
		
		if(myLinks != null && !myLinks.isEmpty()) {
			return myLinks;
		}
		
		return null;
	}
	
	public static List<String> GetHashtagsInText(String textToCheck) {
		Extractor twitterExtractor = new Extractor();
		List<String> myHashtags = twitterExtractor.extractHashtags(textToCheck);
		
		if(myHashtags != null && !myHashtags.isEmpty()) {
			return myHashtags;
		}
		
		return null;
	}
	
	public static class UpdateStatus extends AsyncTask<Void, Integer, Boolean> {
		Context myContext;
		String myUpdate;
		Boolean myFacebook;
		FacebookAuthenticationHelper myFbAuth;
		Boolean myTwitter;
		Boolean myBitly;
		Integer myService;
		
		public UpdateStatus(Context theContext, Integer theService, String theUpdate, Object theAuthHelper, Boolean bitly) {
			myContext = theContext;
			myUpdate = theUpdate;
			myService = theService;
			if(myService == FACEBOOK_UPDATE) {
				myFacebook = true;
				myTwitter = false;
				myFbAuth = (FacebookAuthenticationHelper)theAuthHelper;
			} else if(myService == TWITTER_UPDATE) {
				myTwitter = true;
				myFacebook = false;
				myFbAuth = null;
			}
			myBitly = bitly;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			NotificationManager mNotificationManager = 
					(NotificationManager) myContext.getSystemService(Context.NOTIFICATION_SERVICE);
			
			NotificationCompat.Builder mNotificationFacebook = 
					new NotificationCompat.Builder(myContext)
						.setSmallIcon(R.drawable.ic_stat_rapido)
						.setContentTitle(myContext.getString(R.string.alert_title))
						.setContentText(myContext.getString(R.string.alert_posting_facebook))
						.setContentIntent(MainActivity.genericPendingIntent);
			
			NotificationCompat.Builder mNotificationTwitter = 
					new NotificationCompat.Builder(myContext)
						.setSmallIcon(R.drawable.ic_stat_rapido)
						.setContentTitle(myContext.getString(R.string.alert_title))
						.setContentText(myContext.getString(R.string.alert_posting_twitter))
						.setContentIntent(MainActivity.genericPendingIntent);
			
			NotificationCompat.Builder mNotificationBitly = 
					new NotificationCompat.Builder(myContext)
						.setSmallIcon(R.drawable.ic_stat_rapido)
						.setContentTitle(myContext.getString(R.string.alert_title))
						.setContentText(myContext.getString(R.string.alert_posting_shortening_bitly))
						.setContentIntent(MainActivity.genericPendingIntent);
			
			if(myBitly) {
				mNotificationManager.notify(MainActivity.BITLY_NOTIFICATION, mNotificationBitly.getNotification());
				new BitlyHelper.ShortenAndSend(myContext, myService, myUpdate, myFbAuth).execute();
			} else {
				if (myFacebook) {
					try {
						mNotificationManager.notify(MainActivity.FACEBOOK_NOTIFICATION, mNotificationFacebook.getNotification());
						new FacebookHelper.UpdateStatus(myContext, myFbAuth, myUpdate).execute();
						return true;
					} catch (Exception e) {
						Toast.makeText(myContext, "Facebook message not sent, try again", Toast.LENGTH_SHORT).show();
						Log.i(LOG_TAG, "Exception: " + e.getMessage());
					}
				}
	
				if (myTwitter) {
					try {
						mNotificationManager.notify(MainActivity.TWITTER_NOTIFICATION, mNotificationTwitter.getNotification());
						Log.i(LOG_TAG, "Trying to send tweet");
						new TwitterHelper.SendTweet(myContext, myUpdate).execute();
						Log.i(LOG_TAG, "Tried to tweet.");
						return true;
					} catch (Exception e) {
						Toast.makeText(myContext, "Twitter message not sent, try again", Toast.LENGTH_SHORT).show();
						Log.i(LOG_TAG, "Exception: " + e.getMessage());
					}
				}
			}
			
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			// TODO: Move this....
			//Toast.makeText(myContext, result ? "Message posted successfully!" : myContext.getString(R.string.error_not_posted), Toast.LENGTH_SHORT).show();
		}
	}
}