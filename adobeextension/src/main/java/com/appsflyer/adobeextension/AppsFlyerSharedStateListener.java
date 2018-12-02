package com.appsflyer.adobeextension;

import android.util.Log;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.adobe.marketing.mobile.ExtensionListener;

import java.util.Map;

import static com.appsflyer.adobeextension.AppsFlyerAdobeExtension.AFEXTENSION;

public class AppsFlyerSharedStateListener extends ExtensionListener {

    public AppsFlyerSharedStateListener(final ExtensionApi extension, final String type, final String source) {
        super(extension,source,type);
    }

    @Override
    public void hear(final Event event) {
        ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
            @Override
            public void error(final ExtensionError extensionError) {
                Log.e(AFEXTENSION, "error receiving sharedState event: "+ extensionError.getErrorName());
            }
        };

        Map<String, Object> eventData = event.getEventData();
        String stateOwner = (String) eventData.get("stateowner");

        if ("com.adobe.module.configuration".equals(stateOwner)) {
            Map<String, Object> configurationSharedState = getParentExtension().getApi().getSharedEventState("com.adobe.module.configuration", event, errorCallback);

            if (configurationSharedState != null) {
                String appsFlyerDevKey = configurationSharedState.get("appsFlyerDevKey").toString(); //todo validate no null
                if (appsFlyerDevKey != null) {
                    boolean isDebug = (boolean) configurationSharedState.get("appsFlyerIsDebug");
                    getParentExtension().handleConfigurationEvent(appsFlyerDevKey, isDebug);
                } else {
                    Log.e(AFEXTENSION,"Cannot initialize AppsFlyer tracking without a valid DevKey");
                }
            } else {
                Log.e(AFEXTENSION, "Cannot initialize Appsflyer tracking: null configuration json");
            }
        }
    }

    @Override
    protected AppsFlyerAdobeExtension getParentExtension() {
        return (AppsFlyerAdobeExtension)super.getParentExtension();
    }

    @Override
    public void onUnregistered() {
        super.onUnregistered();
    }
}