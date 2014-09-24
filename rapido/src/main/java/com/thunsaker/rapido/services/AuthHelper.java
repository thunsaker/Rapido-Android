package com.thunsaker.rapido.services;

public class AuthHelper {
    // Facebook
    public static final String FACEBOOK_AUTH_ID = System.getenv("RAPIDO_FACE_ID");

    // Twitter
    public final static String TWITTER_KEY = System.getenv("RAPIDO_TWIT_KEY");
    public final static String TWITTER_SECRET = System.getenv("RAPIDO_TWIT_SECRET");
    public final static String TWITTER_CALLBACK_URL = "http://127.0.0.1/twitter";

    // Bit.ly
    public final static String BITLY_CLIENT_ID = System.getenv("RAPIDO_BIT_ID");
    public final static String BITLY_CLIENT_SECRET = System.getenv("RAPIDO_BIT_SECRET");
    public final static String BITLY_REDIRECT_URL = "http://thomashunsaker.com/apps/rapido";

    public final static String FOURSQUARE_CLIENT_ID = System.getenv("RAPIDO_FOUR_ID");
    public final static String FOURSQUARE_CLIENT_SECRET = System.getenv("RAPIDO_FOUR_SECRET");
    public final static String FOURSQUARE_CALLBACK_URL = "http://127.0.0.1/foursquare";
}
