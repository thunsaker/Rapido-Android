package com.thunsaker.rapido;

import java.io.IOException;
import java.net.MalformedURLException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.Facebook.ServiceListener;
import com.facebook.android.FacebookError;

public class FacebookAuthenticationHelper {
	final String TAG = getClass().getName();
	public static Facebook facebook = new Facebook("YOUR_FACEBOOK_KEY");
	private Activity myActivity;
	private Context myContext;
	
	public FacebookAuthenticationHelper(Activity activity, Context context) {
		myActivity = activity;
		myContext = context;
	}

	// Authentication
	public void AuthenticateWithFacebook() {
		facebook.authorize(myActivity, new String[] { "publish_stream" }, 
			new DialogListener() {
	            @Override
	            public void onComplete(Bundle values) {
	            	PreferencesHelper.setFacebookKey(myContext, facebook.getAccessToken());
	            	PreferencesHelper.setFacebookExpiration(myContext, facebook.getAccessExpires());
	            	PreferencesHelper.setFacebookEnabled(myContext, true);
	            }

	            // Add handling for a user rejecting the permissions
	            @Override
	            public void onFacebookError(FacebookError error) {
	            	PreferencesHelper.setFacebookEnabled(myContext, false);
	            	Log.d(TAG, error.getMessage());
	            }

	            @Override
	            public void onError(DialogError e) {
	            	PreferencesHelper.setFacebookEnabled(myContext, false);
	            	Log.d(TAG, e.getMessage());
	            }

	            @Override
	            public void onCancel() {
	            	PreferencesHelper.setFacebookEnabled(myContext, false);
	            	Log.d(TAG, "Cancelled Facebook Auth");
	            }
	        });
	}
    
    public void SetAccessToken(String key) {
		facebook.setAccessToken(key);
	}
    
    public void SetAuthorizeCallback(int requestCode, int resultCode, Intent data){
    	facebook.authorizeCallback(requestCode, resultCode, data);
    }
    
    public void SetAccessExpires(Long expires) {
    	facebook.setAccessExpires(expires);
    }
    
    public void ExtendAccessTokenIfNeeded(ServiceListener myServiceListener){
    	facebook.extendAccessTokenIfNeeded(myContext, myServiceListener);
    }
    
    public Boolean IsSessionValid(){
    	return facebook.isSessionValid();
    }
    
    public String SignOut() {
    	try {
			facebook.logout(myContext);
			PreferencesHelper.setFacebookExpiration(myContext, Long.valueOf("0"));
			PreferencesHelper.setFacebookEnabled(myContext, false);
			PreferencesHelper.setFacebookKey(myContext, "");
			return "You have been signed out of Facebook";
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return "Malformed URL, try again";
		} catch (IOException e) {
			e.printStackTrace();
			return "A problem occured while signing out, try again";
		}
    }
}
