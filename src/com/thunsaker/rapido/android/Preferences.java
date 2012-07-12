package com.thunsaker.rapido.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.util.Log;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class Preferences extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {
	final String TAG = getClass().getName();
	private boolean useLogo = true;
    private boolean showHomeUp = true;
    
    public FacebookAuthenticationHelper fbHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getPreferenceManager().setSharedPreferencesName(PreferencesHelper.PREFS_NAME);
		addPreferencesFromResource(R.xml.preferences);
		
		if(PreferencesHelper.getFacebookEnabled(getApplicationContext())) {
			CheckBoxPreference fbEnabled = (CheckBoxPreference)findPreference("rapido_facebook_enabled");
		}
		
        final com.actionbarsherlock.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(showHomeUp);
        ab.setDisplayUseLogoEnabled(useLogo);
        
        fbHelper = new FacebookAuthenticationHelper(Preferences.this, getApplicationContext());
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
	    // Set up a listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    // Unregister the listener whenever a key changes
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fbHelper.SetAuthorizeCallback(requestCode, resultCode, data);
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(getString(R.string.prefs_facebook_enabled).equals(key)) {
			if(PreferencesHelper.getFacebookEnabled(getApplicationContext())) {
				fbHelper.AuthenticateWithFacebook();
			} else {
				fbHelper.SignOut();
				PreferencesHelper.setFacebookKey(getApplicationContext(), null);
			}
		} else if (getString(R.string.prefs_twitter_enabled).equals(key)) {
			if(PreferencesHelper.getTwitterEnabled(getApplicationContext())) {
				startActivity(new Intent(getApplicationContext(), TwitterAuthorizationActivity.class));
			} else {
				Log.i(TAG, "Wiping twitter user tokens");
				PreferencesHelper.setTwitterToken(getApplicationContext(), null);
				PreferencesHelper.setTwitterSecret(getApplicationContext(), null);
				
			}
		}
	}
}
