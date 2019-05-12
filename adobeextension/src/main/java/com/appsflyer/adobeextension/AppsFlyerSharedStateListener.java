package com.appsflyer.adobeextension;

import android.util.Log;
import java.util.Map;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.adobe.marketing.mobile.ExtensionListener;

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
            try {
                if (configurationSharedState != null) {
                    if (!configurationSharedState.isEmpty() && (configurationSharedState.get("appsFlyerDevKey") != null)) {
                        String appsFlyerDevKey = configurationSharedState.get("appsFlyerDevKey").toString();
                        boolean isDebug = false;
                        boolean shouldTrackAttr = false;

                        if (configurationSharedState.get("appsFlyerIsDebug") != null) {
                            isDebug = (boolean) configurationSharedState.get("appsFlyerIsDebug");
                        }

                        if (configurationSharedState.get("appsFlyerTrackAttrData") != null) {
                            shouldTrackAttr = (boolean) configurationSharedState.get("appsFlyerTrackAttrData");
                        }

                        getParentExtension().handleConfigurationEvent(appsFlyerDevKey, isDebug, shouldTrackAttr);
                    } else {
                        Log.e(AFEXTENSION, "Cannot initialize AppsFlyer tracking without a valid DevKey");
                    }
                } else {
                    Log.e(AFEXTENSION, "Cannot initialize Appsflyer tracking: null configuration json");
                }
            } catch (NullPointerException npx) {
                Log.e(AFEXTENSION, "Exception while casting devKey to String: "+npx);
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