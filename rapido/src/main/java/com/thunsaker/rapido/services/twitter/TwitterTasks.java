package com.thunsaker.rapido.services.twitter;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.rapido.R;
import com.thunsaker.rapido.app.RapidoApp;
import com.thunsaker.rapido.classes.api.twitter.events.TwitterConnectedEvent;
import com.thunsaker.rapido.util.PreferencesHelper;

import java.io.IOException;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

public class TwitterTasks {
    private static final String LOG_TAG = "TwitterTasks";

    @Inject
    EventBus mBus;

    @Inject @ForApplication
    Context mContext;

    TwitterClient mTwitterClient;

    public TwitterTasks(RapidoApp app) {
        app.inject(this);
        mTwitterClient = new TwitterClient(app);
    }

    public class TokenFetcher extends AsyncTask<Void, Integer, Boolean> {
        OAuthGetAccessToken myOauthGetAccessToken;

        public TokenFetcher(OAuthGetAccessToken theGetAccessToken) {
            myOauthGetAccessToken = theGetAccessToken;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            OAuthCredentialsResponse myResponse = null;
            String token = PreferencesHelper.getTwitterToken(mContext);

            if(token == null || token.equalsIgnoreCase("")) {
                try {
                    myResponse = myOauthGetAccessToken.execute();
                } catch (IOException e) {
                    Log.d(LOG_TAG, "IOException");
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.d(LOG_TAG, "Exception");
                    e.printStackTrace();
                }

                Log.d(LOG_TAG, "Response: " + myResponse);

                if(myResponse != null && myResponse.token != null && myResponse.tokenSecret != null) {

                    PreferencesHelper.setTwitterToken(mContext, myResponse.token);
                    PreferencesHelper.setTwitterSecret(mContext, myResponse.tokenSecret);
                    PreferencesHelper.setTwitterEnabled(mContext, true);
                    PreferencesHelper.setTwitterConnected(mContext, true);
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            String resultMessage = mContext.getString(R.string.twitter_auth_failed);

            if(PreferencesHelper.getTwitterConnected(mContext))
                resultMessage = mContext.getString(R.string.twitter_auth_connected);

            Toast.makeText(mContext, resultMessage, Toast.LENGTH_SHORT).show();
            mBus.post(new TwitterConnectedEvent(result, resultMessage));
        }
    }

//    public class SendDM extends AsyncTask<Void, Integer, DMSendEvent> {
//        String myMessage;
//        public SendDM(String message) {
//            myMessage = message;
//        }
//
//        @Override
//        protected DMSendEvent doInBackground(Void... params) {
//            try {
//                return mTwitterClient.sendDM("rapidoApp", myMessage);
//            } catch (TwitterException e) {
//                e.printStackTrace();
//                return new DMSendEvent(false, e.getMessage(), null);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return new DMSendEvent(false, e.getMessage(), null);
//            }
//        }
//
//        @Override
//        protected void onPostExecute(DMSendEvent dMSendEvent) {
//            Log.d(LOG_TAG, "Posting to the bus...");
//            mBus.post(dMSendEvent);
//        }
//    }

//    public class FollowUser extends AsyncTask<Void, Integer, TwitterFollowingEvent> {
//        String myUser;
//        public FollowUser(String user) {
//            myUser = user;
//        }
//
//        @Override
//        protected TwitterFollowingEvent doInBackground(Void... params) {
//            try {
//                return mTwitterClient.followUser("RapdioApp");
//            } catch (TwitterException e) {
//                e.printStackTrace();
//                return new TwitterFollowingEvent(false, e.getMessage(), null, null);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return new TwitterFollowingEvent(false, e.getMessage(), null, null);
//            }
//        }
//
//        @Override
//        protected void onPostExecute(TwitterFollowingEvent twitterFollowingEvent) {
//            mBus.post(twitterFollowingEvent);
//        }
//    }
}