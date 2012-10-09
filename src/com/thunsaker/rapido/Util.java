package com.thunsaker.rapido;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.thunsaker.rapido.classes.ShortUrl;
import com.twitter.Extractor;

public class Util {
	private static final String LOG_TAG = "Util";
	private final int MENU_LOGOUT = 3;

	final String BITLY_DEFAULT_USERNAME = "YOUR_BITLY_USERNAME";
	final String BITLY_DEFAULT_APIKEY = "YOUR_BITLY_API";
	final String BITLY_BASE_URL = "http://api.bit.ly/v3/";

	public static String contentType = "json/application";

	private ShortUrl shortenUrl(String urlToShorten, Context myContext) {
		try {
			if (Util.HasInternet(myContext)) {
				String longUrl = URLEncoder.encode(urlToShorten);
				ShortUrl theShortenedUrl = new ShortUrl();

				if (longUrl != "") {
					theShortenedUrl = getShortUrl(longUrl);

					if (theShortenedUrl != null
							&& theShortenedUrl.getUrl() != "") {
						// Url shortened properly
						return theShortenedUrl;

						// TODO: Replace the long url in the text
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

	// Shorten the URL
	private ShortUrl getShortUrl(String l) {
		try {
			ShortUrl s = new ShortUrl();

			// get data from request
			// TODO: Use my api key for now...
			String shortenRequestUrl = String.format(
					"%sshorten?login=%s&apiKey=%s&longUrl=%s&format=json",
					BITLY_BASE_URL, BITLY_DEFAULT_USERNAME,
					BITLY_DEFAULT_APIKEY, l);
			String jsonShortUrlResponse = getHttpResponse(shortenRequestUrl,
					contentType, "");

			try {
				if (jsonShortUrlResponse != null) {
					JsonParser jParser = new JsonParser();
					JsonObject jObject = (JsonObject) jParser
							.parse(jsonShortUrlResponse);

					if (jObject != null) {
						if (Integer.parseInt(jObject.get("status_code")
								.toString()) == 200
								&& jObject.get("status_txt").toString() == "OK") {
							JsonObject jObjectShortUrl = jObject
									.getAsJsonObject("data");
							Gson gson = new Gson();
							ShortUrl myShortUrl = gson.fromJson(
									jObjectShortUrl, ShortUrl.class);
							s = myShortUrl;
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
			Log.e(LOG_TAG, "logout() - Exception: " + ex.getMessage());
			return null;
		}
	}

	static String getHttpResponse(String url, String contentType, String accepts) {
		Log.i(LOG_TAG, "getHttp : " + url);
		String result = "";
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		HttpResponse response;

		try {
			response = httpclient.execute(httpget);
			// Log.i(LOG_TAG, response.getStatusLine().toString());
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				result = convertStreamToString(instream);

				// DEBUG: Remove this
				Log.i(LOG_TAG, String.format("Data Service Partial Result: %s",
						result));
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
}