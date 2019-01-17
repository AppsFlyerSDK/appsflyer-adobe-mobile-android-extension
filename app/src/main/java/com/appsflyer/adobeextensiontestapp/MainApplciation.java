package com.appsflyer.adobeextensiontestapp;

import android.app.Application;
import android.util.Log;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.Lifecycle;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.Signal;
import com.adobe.marketing.mobile.UserProfile;
import com.appsflyer.adobeextension.AppsFlyerAdobeExtension;
import com.appsflyer.adobeextension.AppsFlyerExtensionCallbacksListener;

import java.util.Map;

public class MainApplciation extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        MobileCore.setApplication(this);
        MobileCore.setLogLevel(LoggingMode.DEBUG);
        try {
//            AppsFlyerAdobeExtension.setApplication(this);
            AppsFlyerAdobeExtension.registerExtension();
            Identity.registerExtension();
            Lifecycle.registerExtension();
            Signal.registerExtension();
            UserProfile.registerExtension();
            MobileCore.start(new AdobeCallback() {
                @Override
                public void call(Object o) {
                    MobileCore.configureWithAppID("launch-EN8f1076320a5f4a74aa0e826223d7af2d-development");
                }
            });

            AppsFlyerAdobeExtension.registerAppsFlyerExtensionCallbacks(new AppsFlyerExtensionCallbacksListener() {
                @Override
                public void onCallbackReceived(Map<String, String> callback) {
                    Log.d("AppsFlyerCallbacks", callback.toString());
                }

                @Override
                public void onCallbackError(String errorMessage) {

                }
            });

        } catch (Exception ex) {
            Log.d("AdobeException: ",ex.toString());
        }
    }
}
