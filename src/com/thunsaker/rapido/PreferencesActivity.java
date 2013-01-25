package com.thunsaker.rapido;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class PreferencesActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {
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
        
        fbHelper = new FacebookAuthenticationHelper(PreferencesActivity.this, getApplicationContext());
        
        Preference clear_prefs = findPreference(getString(R.string.prefs_clear_accounts));
        clear_prefs.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				ClearTwitterPreferences();
				ClearFacebookPreferences();
				ClearBitlyPreferences();
				Toast.makeText(getApplicationContext(), "All Application Settings Cleared", Toast.LENGTH_SHORT).show();
				return false;
			}
        });
        
        final Preference rateRapido = (Preference)findPreference(getString(R.string.prefs_about_rate));
        rateRapido.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.thunsaker.rapido")));
				return false;
			}
		});
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
		if(getString(R.string.prefs_facebook_connected).equals(key)) {
			if(PreferencesHelper.getFacebookConnected(getApplicationContext())) {
				fbHelper.AuthenticateWithFacebook();
			} else {
				new FacebookAuthenticationHelper.SignOut(getApplicationContext()).execute();
				ClearFacebookPreferences();
			}
		} else if (getString(R.string.prefs_twitter_connected).equals(key)) {
			if(PreferencesHelper.getTwitterConnected(getApplicationContext())) {
				startActivity(new Intent(getApplicationContext(), TwitterAuthorizationActivity.class));
			} else {
				Log.i(TAG, "Wiping twitter user tokens");
				ClearTwitterPreferences();
			}
		} else if (getString(R.string.prefs_bitly_connected).equals(key)) {
			if(PreferencesHelper.getBitlyConnected(getApplicationContext())) {
				startActivity(new Intent(getApplicationContext(), BitlyAuthorizationActivity.class));
			} else {
				Log.i(TAG, "Wiping bitly user tokens");
				ClearBitlyPreferences();
			}
		}
	}
	
	public void ClearTwitterPreferences() {
		PreferencesHelper.setTwitterConnected(getApplicationContext(), false);
		PreferencesHelper.setTwitterEnabled(getApplicationContext(), false);
		PreferencesHelper.setTwitterToken(getApplicationContext(), null);
		PreferencesHelper.setTwitterSecret(getApplicationContext(), null);
	}
	
	public void ClearFacebookPreferences() {
		PreferencesHelper.setFacebookConnected(getApplicationContext(), false);
		PreferencesHelper.setFacebookEnabled(getApplicationContext(), false);
		PreferencesHelper.setFacebookKey(getApplicationContext(), null);
	}
	
	public void ClearBitlyPreferences() {
		PreferencesHelper.setBitlyConnected(getApplicationContext(), false);
		PreferencesHelper.setBitlyToken(getApplicationContext(), null);
		PreferencesHelper.setBitlyApiKey(getApplicationContext(), null);
		PreferencesHelper.setBitlyLogin(getApplicationContext(), null);
	}
}