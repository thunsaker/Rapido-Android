package com.thunsaker.rapido.services.twitter;

import android.content.Context;
import android.text.util.Linkify;

import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.rapido.app.RapidoApp;
import com.thunsaker.rapido.util.PreferencesHelper;
import com.twitter.Extractor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import twitter4j.Twitter;
import twitter4j.auth.AccessToken;

public class TwitterClient {
	private static final String LOG_TAG = "TwitterClient";

    @Inject
    @ForApplication
    Context mContext;

    @Inject
    Twitter mTwitter;

    public TwitterClient(RapidoApp app) {
        app.inject(this);
    }

    private void setTwitterCredentials() {
        String userToken = PreferencesHelper.getTwitterToken(mContext);
        String userSecret = PreferencesHelper.getTwitterSecret(mContext);
        mTwitter.setOAuthAccessToken(new AccessToken(userToken, userSecret));
    }

    public static Linkify.TransformFilter twitterMentionFilter = new Linkify.TransformFilter() {
        @Override
        public String transformUrl(Matcher match, String url) {
            return match.group(1);
        }
    };

    public static Pattern twitterMentionPattern = Pattern.compile("@([A-Za-z0-9_-]+)");
    public static String twitterUrlScheme = "http://twitter.com/";

    public static List<String> GetLinksInText(String textToCheck) {
        Extractor twitterExtractor = new Extractor();
        List<String> myLinks = twitterExtractor.extractURLs(textToCheck);

        if(myLinks != null && !myLinks.isEmpty()) {
            return myLinks;
        }

        return null;
    }
}