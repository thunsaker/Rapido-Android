package com.thunsaker.rapido.app;

import android.app.NotificationManager;
import android.content.Context;
import android.location.LocationManager;

import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.android.common.dagger.AndroidApplicationModule;
import com.thunsaker.rapido.services.AuthHelper;
import com.thunsaker.rapido.services.twitter.TwitterClient;
import com.thunsaker.rapido.services.twitter.TwitterTasks;
import com.thunsaker.rapido.ui.MainActivity;
import com.thunsaker.rapido.ui.TwitterAuthorizationActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

import static android.content.Context.LOCATION_SERVICE;

@Module(
        complete = true,
        library = true,
        addsTo = AndroidApplicationModule.class,
        injects = {
                RapidoApp.class,
                MainActivity.class,
                TwitterClient.class,
                TwitterTasks.class,
                TwitterAuthorizationActivity.class
        }
)

public class RapidoAppModule {
    public RapidoAppModule() {}

    @Provides
    @Singleton
    NotificationManager providesNotificationManager(@ForApplication Context myContext) {
        return (NotificationManager) myContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Provides
    @Singleton
    LocationManager provideLocationManager(@ForApplication Context context) {
        return (LocationManager) context.getSystemService(LOCATION_SERVICE);
    }

    @Provides
    @Singleton
    Twitter providesTwitter() {
        Twitter twitter = TwitterFactory.getSingleton();
        twitter.setOAuthConsumer(AuthHelper.TWITTER_KEY, AuthHelper.TWITTER_SECRET);
        return twitter;
    }

// TODO: Foursquare Service
//    @Provides
//    @Singleton
//    FoursquareService providesFoursquareService() {
//        RequestInterceptor requestInterceptor = new RequestInterceptor() {
//            @Override
//            public void intercept(RequestFacade request) {
//                request.addQueryParam("v", FoursquarePrefs.CURRENT_API_DATE);
//                request.addQueryParam("m", FoursquarePrefs.API_MODE_FOURSQUARE);
//            }
//        };
//
////        GsonBuilder builder = new GsonBuilder();
////        builder.registerTypeAdapter()
////        Gson myGson = new Gson();
//
//        RestAdapter restAdapter = new RestAdapter.Builder()
//                .setEndpoint(FoursquarePrefs.FOURSQUARE_BASE_URL)
//                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
//                .setRequestInterceptor(requestInterceptor)
//                .build();
//
//        return restAdapter.create(FoursquareService.class);
//    }
//
//    @Provides
//    @Singleton
//    SwarmService providesSwarmService() {
//        RequestInterceptor requestInterceptor = new RequestInterceptor() {
//            @Override
//            public void intercept(RequestFacade request) {
//                request.addQueryParam("v", FoursquarePrefs.CURRENT_API_DATE);
//                request.addQueryParam("m", FoursquarePrefs.API_MODE_SWARM);
//            }
//        };
//
//        RestAdapter restAdapter = new RestAdapter.Builder()
//                .setEndpoint(FoursquarePrefs.FOURSQUARE_BASE_URL)
//                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
//                .setRequestInterceptor(requestInterceptor)
//                .build();
//
//        return restAdapter.create(SwarmService.class);
//    }
//
//    @Provides
//    @Singleton
//    FoursquareTasks providesFoursquareTasks(@ForApplication Context myContext) {
//        return new FoursquareTasks((SoupApp)myContext);
//    }
}
