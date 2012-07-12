package com.thunsaker.rapido.android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class MainActivity extends SherlockActivity {
	final String TAG = getClass().getName();
    private boolean useLogo = true;
    private boolean showHomeUp = false;
	
	private TextView mTextView;
    private EditText mEditText;
    private ToggleButton mToggleButtonFacebook;
    private ToggleButton mToggleButtonTwitter;
    private Button mButtonSend;
    
    private Boolean facebookEnabled = false;
    private Boolean twitterEnabled = false;
    private Boolean submitOnEnter = false;
    
    private String FACEBOOK_KEY = "";
    private String TWITTER_TOKEN = "";
    private String TWITTER_TOKEN_SECRET = "";
    
    private FacebookAuthenticationHelper fbAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final com.actionbarsherlock.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(showHomeUp);
        ab.setDisplayUseLogoEnabled(useLogo);
        
        fbAuth = new FacebookAuthenticationHelper(this, getApplicationContext());
        
        // Get Prefs
        facebookEnabled = PreferencesHelper.getFacebookEnabled(getApplicationContext());
        twitterEnabled = PreferencesHelper.getTwitterEnabled(getApplicationContext());
        submitOnEnter = PreferencesHelper.getSendOnEnterEnabled(getApplicationContext());
        
        // Get Keys
        FACEBOOK_KEY = PreferencesHelper.getFacebookKey(getApplicationContext());
        long expires = PreferencesHelper.getFacebookExpiration(getApplicationContext());
        if(FACEBOOK_KEY != null) {
        	fbAuth.SetAccessToken(FACEBOOK_KEY);
            
	        if(expires != 0) {
	            fbAuth.SetAccessExpires(expires);
	        }
	        if(!fbAuth.IsSessionValid()) {
	        	fbAuth.AuthenticateWithFacebook();
	        }
        }
        
        TWITTER_TOKEN = PreferencesHelper.getTwitterToken(getApplicationContext());
        TWITTER_TOKEN_SECRET = PreferencesHelper.getTwitterSecret(getApplicationContext());
        
        mTextView = (TextView)findViewById(R.id.TextViewCount);
        mEditText = (EditText)findViewById(R.id.EditTextUpdate);
        mEditText.addTextChangedListener(mTextEditorWatcher);
        
        mToggleButtonFacebook = (ToggleButton)findViewById(R.id.ToggleButtonFacebook);
        mToggleButtonFacebook.setOnCheckedChangeListener(mFbCheckedChangeListener);
        
        mToggleButtonTwitter = (ToggleButton)findViewById(R.id.ToggleButtonTwitter);
        mToggleButtonTwitter.setOnCheckedChangeListener(mTwitterCheckedChangeListener);
        
        mButtonSend = (Button)findViewById(R.id.ButtonSend);
        mButtonSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		         try {
		        	 String update = mEditText.getText().toString();
		        	 int limit = 1000;
		        	 Boolean isTwitter = mToggleButtonTwitter.isChecked();
		        	 Boolean isFacebook = mToggleButtonFacebook.isChecked();
		        	 
		        	 if(isTwitter || isFacebook) {
			        	 if(isTwitter) {
			        		 limit = 140;
			        	 }
			        	 
			        	 if(update.length() > 0 && update.length() < limit) {
			        		if(isFacebook) {
				                String response = fbAuth.facebook.request("me");
				                Bundle parameters = new Bundle();
				                parameters.putString("message", update);
				                parameters.putString("description", "Posting from Rapido for Android");
				                response = fbAuth.facebook.request("me/feed", parameters, "POST");
				                if (response == null || response.equals("") || response.equals("false")) {
				                	Toast.makeText(getApplicationContext(), R.string.error_not_posted, Toast.LENGTH_SHORT).show();
				                } else {
				                	// Toast.makeText(getApplicationContext(), R.string.alert_posted_facebook, Toast.LENGTH_SHORT).show();
				                	if(!isTwitter) {
				                		CloseApp();
				                	}
				                }
			        		}
			        		
			        		if(isTwitter) {
			        			try {
			        				Boolean twitResult = TwitterHelper.sendTweet(getApplicationContext(), update);
			        				if(twitResult) {
			        					// Toast.makeText(getApplicationContext(), R.string.alert_posted_twitter, Toast.LENGTH_SHORT).show();
			        					CloseApp();
			        				} else {
			        					Toast.makeText(getApplicationContext(), "Twitter message not sent, try again", Toast.LENGTH_SHORT).show();	
			        				}
			        			} catch (Exception e) {
			        				Toast.makeText(getApplicationContext(), "Twitter message not sent, try again", Toast.LENGTH_SHORT).show();
			        				Log.i(TAG, "Exception: " + e.getMessage());
								}
			        		}
			        	 } else {
			        		 Toast.makeText(getApplicationContext(), R.string.error_message_too_long, Toast.LENGTH_SHORT).show();
			        	 }
		        	 } else {
		        		 Toast.makeText(getApplicationContext(), R.string.error_no_services_selected, Toast.LENGTH_SHORT).show();
		        	 }
		         } catch(Exception e) {
		             e.printStackTrace();
		         }
			}
		});
    }
    
    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
    	getSupportMenuInflater().inflate(R.menu.activity_main, menu);
    	
    	final MenuItem menu_settings = (MenuItem) menu.findItem(R.id.menu_settings);
    	menu_settings.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
				// Toast.makeText(getApplicationContext(), "Settings here!", Toast.LENGTH_SHORT).show();
				Intent preferencesIntent = 
						new Intent(getApplicationContext(), 
								Preferences.class);
				startActivity(preferencesIntent);
				return false;
			}
		});

    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
        fbAuth.ExtendAccessTokenIfNeeded(null);
    }
    
    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
    	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    	}
    	
    	public void onTextChanged(CharSequence s, int start, int before, int count) {
    		updateCharacterCount(s.length());
    	}

		public void afterTextChanged(Editable s) {
    	}
    };
    
    // Twitter Toggle Button
    private final OnCheckedChangeListener mTwitterCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked == true ) {
				updateCharacterCount(-1);
				if(TWITTER_TOKEN != null) {
					return;
				} else {
					// Launch Twitter Activity
					Intent twitterAuth = new Intent(getApplicationContext(), TwitterAuthorizationActivity.class);
					startActivity(twitterAuth);
				}
			}
		}
	};
	
	// Facebook Toggle Button
	private final OnCheckedChangeListener mFbCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked == true) {
				updateCharacterCount(-1);
				if(FACEBOOK_KEY != null && PreferencesHelper.getFacebookEnabled(getApplicationContext())) {
					return;
				} else {
					fbAuth.AuthenticateWithFacebook();
				}
			}
		}
	};
    
	private void updateCharacterCount(int length) {
		int limit = 1000;
		if(length == -1) {
			length = mEditText.length();
		}
		
		if(mToggleButtonTwitter.isChecked()) {
			limit = 140;
		}

		if(length > limit) {
			mTextView.setTextColor(Color.RED);
		} else {
			mTextView.setTextColor(Color.BLACK);
		}
		mTextView.setText(String.valueOf(length - limit));
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fbAuth.SetAuthorizeCallback(requestCode, resultCode, data);
    }
	
	public void CloseApp(){
		Toast.makeText(getApplicationContext(), "Message posted successfully!", Toast.LENGTH_SHORT).show();
		mEditText.setText("");
		finish();
	}
}