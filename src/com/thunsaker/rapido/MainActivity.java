package com.thunsaker.rapido;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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
import com.thunsaker.rapido.classes.Draft;

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

	public static String STORAGE_FILENAME = "RAPIDO_STORAGE";
	
	public static String REPOST_FACEBOOK = "RAPIDO_REPOST_FACEBOOK";
	public static String REPOST_TWITTER = "RAPIDO_REPOST_TWITTER";
	public static String REPOST_BOTH = "RAPIDO_REPOST_BOTH";
	private String REPOST_START_TEXT = "RAPIDO_REPOST";
	
	public static String DRAFT_TWITTER = "RAPIDO_DRAFT_TWITTER";
	public static String DRAFT_FACEBOOK = "RAPIDO_DRAFT_FACEBOOK";
	public static String DRAFT_GENERIC = "RAPIDO_DRAFT_GENERIC";

	private FacebookAuthenticationHelper fbAuth;

	static final int DIALOG_NO_INTERNETS_ID = 0;
	
	public static ProgressDialog loadingDialog;
	public static ProgressDialog sendingDialog;
	
	public static final int RAPIDO_NOTIFICATION = 0;
	public static final int FACEBOOK_NOTIFICATION = 1;
	public static final int TWITTER_NOTIFICATION = 2;
	public static final int BITLY_NOTIFICATION = 3;
	public static Intent genericIntent;
	public static PendingIntent genericPendingIntent;

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
			if (Intent.ACTION_SEND.equals(action)) {
				handleRecievedText(intentReceived);
			}
		}
		
		genericIntent = new Intent(getApplicationContext(), MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(genericIntent);
		genericPendingIntent = 
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
	}

	private void handleRecievedText(Intent intent) {
		String sentText = intent.getStringExtra(Intent.EXTRA_TEXT);
		String textToPost = "";
		
		// Check to see if it is a repost, instead of just a received intent.
		if(sentText.startsWith(REPOST_START_TEXT)) {
			if(sentText.startsWith(REPOST_BOTH)) {
				textToPost = sentText.replace(REPOST_BOTH + " ", "");
				mToggleButtonFacebook.setChecked(true);
				mToggleButtonTwitter.setChecked(true);
			} else if (sentText.startsWith(REPOST_FACEBOOK)) {
				textToPost = sentText.replace(REPOST_FACEBOOK + " ", "");
				mToggleButtonFacebook.setChecked(true);
				mToggleButtonTwitter.setChecked(false);
			} else if (sentText.startsWith(REPOST_TWITTER)) {
				textToPost = sentText.replace(REPOST_TWITTER + " ", "");
				mToggleButtonFacebook.setChecked(false);
				mToggleButtonTwitter.setChecked(true);
			}
		} else {
			//String sentTextTitle = intent.getStringExtra(Intent.EXTRA_TITLE);
			String sentTextSubject = 
					intent.getStringExtra(Intent.EXTRA_SUBJECT) != null 
					? String.format("%s - ", intent.getStringExtra(Intent.EXTRA_SUBJECT)) 
							: "";
			textToPost = String.format("%s%s", sentTextSubject, sentText);
		}
		mEditText.setText(textToPost);
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
				EditText myEditText = (EditText) findViewById(R.id.EditTextUpdate);
				String myDraftMessage = myEditText.getText().toString();
				Draft myDraft = new Draft(myDraftMessage);
				MainActivity.saveDraft(MainActivity.DRAFT_GENERIC, myDraft, getApplicationContext());
				Intent preferencesIntent = new Intent(getApplicationContext(), PreferencesActivity.class);
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

			if (Intent.ACTION_SEND.equals(action) && type != null) {
				handleRecievedText(intentReceived);
			} else if(intentReceived.getStringExtra(Intent.EXTRA_TEXT) != null) {
				if(intentReceived.getStringExtra(Intent.EXTRA_TEXT).startsWith(REPOST_START_TEXT)) {
					handleRecievedText(intentReceived);
				}
			}
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
				NotificationManager mNotificationManager = 
						(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				
				NotificationCompat.Builder mNotificationFacebook = 
						new NotificationCompat.Builder(getApplicationContext())
							.setSmallIcon(R.drawable.ic_stat_rapido)
							.setContentTitle(getString(R.string.alert_title))
							.setContentText(getString(R.string.alert_posting_facebook))
							.setContentIntent(genericPendingIntent);
				NotificationCompat.Builder mNotificationTwitter = 
						new NotificationCompat.Builder(getApplicationContext())
							.setSmallIcon(R.drawable.ic_stat_rapido)
							.setContentTitle(getString(R.string.alert_title))
							.setContentText(getString(R.string.alert_posting_twitter))
							.setContentIntent(genericPendingIntent);
				
				ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
				if (activeNetworkInfo != null) {
					String update = mEditText.getText().toString();
					int limit = 1000;
					Boolean isTwitter = mToggleButtonTwitter.isChecked();
					Boolean isFacebook = mToggleButtonFacebook.isChecked();
					Boolean isBitlyEnabled = PreferencesHelper.getBitlyConnected(getApplicationContext());
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
								if(isFacebook) {
									Log.i(TAG, "Starting facebook update");
									mNotificationManager.notify(FACEBOOK_NOTIFICATION, mNotificationFacebook.getNotification());
									new Util.UpdateStatus(getApplicationContext(), Util.FACEBOOK_UPDATE, update, fbAuth, isBitlyEnabled).execute();
								}
								
								if(isTwitter) {
									Log.i(TAG, "Starting twitter update");
									mNotificationManager.notify(TWITTER_NOTIFICATION, mNotificationTwitter.getNotification());
									new Util.UpdateStatus(getApplicationContext(), Util.TWITTER_UPDATE, update, null, isBitlyEnabled).execute();
								}
								
								CloseApp();
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
			EditText myEditText = (EditText) findViewById(R.id.EditTextUpdate); 
			String myDraftMessage = myEditText.getText().toString();
			Draft myDraft = new Draft(myDraftMessage);
			MainActivity.saveDraft(MainActivity.DRAFT_GENERIC, myDraft, getApplicationContext());
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
			EditText myEditText = (EditText) findViewById(R.id.EditTextUpdate); 
			String myDraftMessage = myEditText.getText().toString();
			Draft myDraft = new Draft(myDraftMessage);
			MainActivity.saveDraft(MainActivity.DRAFT_GENERIC, myDraft, getApplicationContext());
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

	public static void saveDraft(String draftKey, Draft myDraft, Context myContext) {
		try {
			if(myDraft != null) {
				FileOutputStream fos = myContext.openFileOutput(STORAGE_FILENAME, Context.MODE_PRIVATE);
				fos.write(myDraft.toString().getBytes());
				fos.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void clearDraft(String draftKey, Context myContext) {
		myContext.deleteFile(STORAGE_FILENAME);
	}

	public static Draft getDraft(String draftKey, Context myContext) {
		try {
			String readString = null;
			FileInputStream fis = myContext.openFileInput(STORAGE_FILENAME);
			fis.read(readString.getBytes());
			fis.close();
			return Draft.GetDraftFromJson(readString);
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
		mEditText.setText("");
		finish();
	}
}