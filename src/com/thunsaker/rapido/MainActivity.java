package com.thunsaker.rapido;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
	private Boolean facebookConnected = false;
	private Boolean twitterConnected = false;
	private Boolean submitOnEnter = false;

	private String FACEBOOK_KEY = "";
	private String TWITTER_TOKEN = "";
	private String TWITTER_TOKEN_SECRET = "";

	private String STORAGE_FILENAME = "RAPIDO_STORAGE";

	private FacebookAuthenticationHelper fbAuth;

	static final int DIALOG_NO_INTERNETS_ID = 0;
	
	public static ProgressDialog loadingDialog;
	public static ProgressDialog sendingDialog;

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

		facebookConnected = PreferencesHelper.getFacebookConnected(getApplicationContext());
		twitterConnected = PreferencesHelper.getTwitterConnected(getApplicationContext());
		submitOnEnter = PreferencesHelper.getSendOnEnterEnabled(getApplicationContext());

		// Get Keys
		FACEBOOK_KEY = PreferencesHelper.getFacebookKey(getApplicationContext());
		long expires = PreferencesHelper.getFacebookExpiration(getApplicationContext());
		if (FACEBOOK_KEY != null) {
			fbAuth.SetAccessToken(FACEBOOK_KEY);

			if (expires != 0)
				fbAuth.SetAccessExpires(expires);
			if (!fbAuth.IsSessionValid())
				fbAuth.AuthenticateWithFacebook();
		}

		TWITTER_TOKEN = PreferencesHelper.getTwitterToken(getApplicationContext());
		TWITTER_TOKEN_SECRET = PreferencesHelper.getTwitterSecret(getApplicationContext());

		mTextView = (TextView) findViewById(R.id.TextViewCount);
		mEditText = (EditText) findViewById(R.id.EditTextUpdate);
		mEditText.addTextChangedListener(mTextEditorWatcher);

		mToggleButtonFacebook = (ToggleButton) findViewById(R.id.ToggleButtonFacebook);
		mToggleButtonFacebook.setOnCheckedChangeListener(mFbCheckedChangeListener);
		if (facebookConnected)
			mToggleButtonFacebook.setChecked(facebookEnabled);

		mToggleButtonTwitter = (ToggleButton) findViewById(R.id.ToggleButtonTwitter);
		mToggleButtonTwitter.setOnCheckedChangeListener(mTwitterCheckedChangeListener);
		if (twitterConnected)
			mToggleButtonTwitter.setChecked(twitterEnabled);

		mButtonSend = (Button) findViewById(R.id.ButtonSend);
		mButtonSend.setOnClickListener(mSendButtonClickListener);

		Intent intentReceived = getIntent();
		if (intentReceived != null) {
			String action = intentReceived.getAction();
			// String type = intentReceived.getType();

			if (Intent.ACTION_SEND.equals(action)) {
				handleRecievedText(intentReceived);
			}
		}
	}

	private void handleRecievedText(Intent intent) {
		String sentText = intent.getStringExtra(Intent.EXTRA_TEXT);
		String sentTextTitle = intent.getStringExtra(Intent.EXTRA_TITLE);
		String sentTextSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT) != null ? String
				.format("%s - ", intent.getStringExtra(Intent.EXTRA_SUBJECT))
				: "";
		mEditText.setText(String.format("%s%s", sentTextSubject, sentText));
		updateCharacterCount(-1);
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);

		final MenuItem menu_settings = (MenuItem) menu
				.findItem(R.id.menu_settings);
		menu_settings.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(
					com.actionbarsherlock.view.MenuItem item) {
				saveDraft();
				Intent preferencesIntent = new Intent(getApplicationContext(), Preferences.class);
				startActivity(preferencesIntent);
				return false;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (fbAuth != null)
			fbAuth.ExtendAccessTokenIfNeeded(null);

		Intent intentReceived = getIntent();
		if (intentReceived != null) {
			String action = intentReceived.getAction();
			String type = intentReceived.getType();

			if (Intent.ACTION_SEND.equals(action) && type != null)
				handleRecievedText(intentReceived);
		}
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

	private final View.OnClickListener mSendButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			try {
				sendingDialog = ProgressDialog.show(
						MainActivity.this, "Please wait...",
						"Updating status...",
						true, // Undefined progress
						true, // Allow canceling of operation
						new OnCancelListener() {
							public void onCancel(
									DialogInterface dialog) {
								Toast.makeText(
										getApplicationContext(),
										getString(R.string.update_abort),
										Toast.LENGTH_SHORT).show();
							}
						});
				
				ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
				if (activeNetworkInfo != null) {
					String update = mEditText.getText().toString();
					int limit = 1000;
					Boolean isTwitter = mToggleButtonTwitter.isChecked();
					Boolean isFacebook = mToggleButtonFacebook.isChecked();
					int updateLength = update.length();

					if (isTwitter || isFacebook) {
						if (updateLength > 0) {
							if (isTwitter) {
								limit = 140;
								
								// If we are over 140 then check to see if we have links that can be shortened.
								if (updateLength > limit) {
									List<String> links = Util.GetLinksInText(update);

									if (links != null && links.size() > 0) {
										int linkCharacters = 0;
										for (String linkString : links) {
											linkCharacters += (linkString.length() - 20);
										}

										updateLength -= linkCharacters;
									}
								}
							}

							if (updateLength < limit) {
								if (isFacebook) {
									new FacebookHelper.UpdateStatus(getApplicationContext(), fbAuth, update, isTwitter).execute();
									CloseApp();
								}

								if (isTwitter) {
									try {
										new TwitterHelper.SendTweet(getApplicationContext(), update).execute();
										
										//Boolean twitResult = TwitterHelper.sendTweet(getApplicationContext(), update);
										CloseApp();
									} catch (Exception e) {
										Toast.makeText(getApplicationContext(), "Twitter message not sent, try again", Toast.LENGTH_SHORT).show();
										Log.i(TAG, "Exception: " + e.getMessage());
									}
								}
							} else {
								Toast.makeText(getApplicationContext(), R.string.error_message_too_long, Toast.LENGTH_SHORT).show();
							}
						} else {
							Toast.makeText(getApplicationContext(), R.string.error_no_message, Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(getApplicationContext(), R.string.error_no_services_selected, Toast.LENGTH_SHORT).show();
					}
				} else {
					// Show Dialog
					showDialog(DIALOG_NO_INTERNETS_ID);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	// Twitter Toggle Button
	private final OnCheckedChangeListener mTwitterCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			updateCharacterCount(-1);
			saveDraft();
			if (isChecked == true) {
				if (TWITTER_TOKEN != null && PreferencesHelper.getTwitterConnected(getApplicationContext())) {
					return;
				} else {
					ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
					if (activeNetworkInfo != null) {
						// Launch Twitter Activity
						Intent twitterAuth = new Intent(getApplicationContext(), TwitterAuthorizationActivity.class);
						startActivity(twitterAuth);
					} else {
						// Show Dialog
						buttonView.setChecked(false);
						showDialog(DIALOG_NO_INTERNETS_ID);
					}
				}
			}
		}
	};

	// Facebook Toggle Button
	private final OnCheckedChangeListener mFbCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			updateCharacterCount(-1);
			saveDraft();
			if (isChecked == true) {
				if (FACEBOOK_KEY != null && PreferencesHelper.getFacebookConnected(getApplicationContext())) {
					return;
				} else {
					ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
					if (activeNetworkInfo != null) {
						fbAuth.AuthenticateWithFacebook();
					} else {
						// Show Dialog
						buttonView.setChecked(false);
						showDialog(DIALOG_NO_INTERNETS_ID);
					}
				}
			}
		}
	};

	private void updateCharacterCount(int length) {
		String text = mEditText.getText().toString();
		int limit = 1000;
		if (length == -1)
			length = mEditText.length();

		if (mToggleButtonTwitter != null && mToggleButtonTwitter.isChecked()) {
			limit = 140;
			if (length > limit) {
				List<String> links = Util.GetLinksInText(text);

				if (links != null && links.size() > 0) {
					int linkCharacters = 0;
					for (String linkString : links) {
						linkCharacters += (linkString.length() - 20);
					}

					length -= linkCharacters;
				}
			}
		}

		if (length > limit) {
			mTextView.setTextColor(Color.RED);
		} else {
			mTextView.setTextColor(Color.BLACK);
		}
		mTextView.setText(String.valueOf(length - limit));
	}

	private void saveDraft() {
		try {
			String draftMessage = mEditText.getText().toString();
			if (draftMessage.length() > 0) {
				FileOutputStream fos = openFileOutput(STORAGE_FILENAME, Context.MODE_PRIVATE);
				fos.write(draftMessage.getBytes());
				fos.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void clearDraft() {
		deleteFile(STORAGE_FILENAME);
	}

	private String getDraft() {
		try {
			String readString = null;
			FileInputStream fis = openFileInput(STORAGE_FILENAME);
			fis.read(readString.getBytes());
			fis.close();
			return readString;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		fbAuth.SetAuthorizeCallback(requestCode, resultCode, data);
		
		//TODO: See if the user cancelled or not, and set the facebook checkbox accordingly
	}

	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {
		AlertDialog alert;
		switch (id) {
		case DIALOG_NO_INTERNETS_ID:
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
			dialogBuilder
					.setMessage(getString(R.string.dialog_no_internets))
					.setCancelable(true)
					.setPositiveButton("ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							});

			alert = dialogBuilder.create();
			break;
		default:
			alert = null;
		}
		return alert;
	}

	public void CloseApp() {
		sendingDialog.dismiss();
		Toast.makeText(getApplicationContext(), "Message posted successfully!", Toast.LENGTH_SHORT).show();
		mEditText.setText("");
		clearDraft();
		finish();
	}
}