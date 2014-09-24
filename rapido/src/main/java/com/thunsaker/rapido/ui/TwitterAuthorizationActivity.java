package com.thunsaker.rapido.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.google.api.client.auth.oauth.OAuthGetTemporaryToken;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.android.common.util.QueryStringParser;
import com.thunsaker.rapido.R;
import com.thunsaker.rapido.app.BaseRapidoActivity;
import com.thunsaker.rapido.app.RapidoApp;
import com.thunsaker.rapido.services.AuthHelper;
import com.thunsaker.rapido.services.twitter.TwitterTasks;
import com.thunsaker.rapido.util.PreferencesHelper;

import javax.inject.Inject;

public class TwitterAuthorizationActivity extends BaseRapidoActivity {
    @Inject
    @ForApplication
    Context mContext;

    final String TAG = "TwitterAuthorizationActivity";

    public static final String REQUEST_URL = "https://api.twitter.com/oauth/request_token";
    public static final String ACCESS_URL = "https://api.twitter.com/oauth/access_token";
    public static final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";

    public ProgressDialog loadingDialog;
    public Boolean isTempTokenSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayUseLogoEnabled(false);
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
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

        loadingDialog = ProgressDialog.show(
                TwitterAuthorizationActivity.this, getString(R.string.dialog_please_wait),
                String.format(getString(R.string.dialog_loading), getString(R.string.twitter)),
                true, // Undefined progress
                true, // Allow canceling of operation
                new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        Toast.makeText(
                                mContext,
                                getString(R.string.auth_cancelled),
                                Toast.LENGTH_SHORT).show();
                    }
                });

        new TempTokenFetcher(TwitterAuthorizationActivity.this, this).execute();
    }

    public void SetupTwitterAuthorization(OAuthHmacSigner theSigner, OAuthCredentialsResponse theTempCredentials){
        try {
            WebView webView = new WebView(this);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setClickable(true);
            webView.setVisibility(View.VISIBLE);
            setContentView(webView);
            final OAuthHmacSigner mySigner = theSigner;

            OAuthAuthorizeTemporaryTokenUrl authorizeUrl = new OAuthAuthorizeTemporaryTokenUrl(AUTHORIZE_URL);
            authorizeUrl.temporaryToken = theTempCredentials.token;
            String authorizationUrl = authorizeUrl.build();
            webView.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageStarted(WebView view, String url,
                                          Bitmap favicon) {
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    if(url.startsWith(AuthHelper.TWITTER_CALLBACK_URL)){
                        try {
                            if(url.contains("oauth_token=")) { // if(url.indexOf("oauth_token=") != -1) {
                                PreferencesHelper.setTwitterToken(mContext, "");
                                PreferencesHelper.setTwitterSecret(mContext, "");

                                String requestToken = extractParamFromUrl(url, "oauth_token");
                                String verifier = extractParamFromUrl(url, "oauth_verifier");
                                mySigner.clientSharedSecret = AuthHelper.TWITTER_SECRET;

                                OAuthGetAccessToken accessToken = new OAuthGetAccessToken(ACCESS_URL);
                                accessToken.transport = new ApacheHttpTransport();
                                accessToken.temporaryToken = requestToken;
                                accessToken.signer = mySigner;
                                accessToken.consumerKey = AuthHelper.TWITTER_KEY;
                                accessToken.verifier = verifier;

                                TwitterTasks twitterTask = new TwitterTasks((RapidoApp)getApplication());
                                twitterTask.new TokenFetcher(accessToken).execute();
                                finish();
                            } else if (url.contains("error=")) { // } else if (url.indexOf("error=") != -1) {
                                view.setVisibility(View.INVISIBLE);
                                Log.d(TAG, "No match: " + url);
                                finish();
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "IOException: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            });

            webView.loadUrl(authorizationUrl);
            webView.requestFocus();
            loadingDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String extractParamFromUrl(String url,String paramName) {
        String queryString = url.substring(url.indexOf("?", 0)+1,url.length());
        QueryStringParser queryStringParser = new QueryStringParser(queryString);
        return queryStringParser.getQueryParamValue(paramName);
    }

    public class TempTokenFetcher extends AsyncTask<Void, Integer, Boolean> {
        Context myContext;
        TwitterAuthorizationActivity myCaller = null;
        final OAuthHmacSigner signer = new OAuthHmacSigner();
        OAuthCredentialsResponse tempCredentials;

        public TempTokenFetcher(Context theContext, TwitterAuthorizationActivity theCaller) {
            myContext = theContext;
            myCaller = theCaller;
        }


        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                signer.clientSharedSecret = AuthHelper.TWITTER_SECRET;

                OAuthGetTemporaryToken tempToken = new OAuthGetTemporaryToken(REQUEST_URL);
                tempToken.transport = new ApacheHttpTransport();
                tempToken.signer = signer;
                tempToken.consumerKey = AuthHelper.TWITTER_KEY;
                tempToken.callback = AuthHelper.TWITTER_CALLBACK_URL;

                tempCredentials = tempToken.execute();
                signer.tokenSharedSecret = tempCredentials.tokenSecret;
            } catch (Exception e) {
                e.printStackTrace();
                isTempTokenSet = false;
            }
            return isTempTokenSet;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            myCaller.SetupTwitterAuthorization(signer, tempCredentials);
        }
    }
}