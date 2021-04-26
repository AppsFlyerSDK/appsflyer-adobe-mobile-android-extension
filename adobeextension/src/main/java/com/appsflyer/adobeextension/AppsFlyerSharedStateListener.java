package com.appsflyer.adobeextension;

import android.util.Log;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.adobe.marketing.mobile.ExtensionListener;

import java.util.Map;

import static com.appsflyer.adobeextension.AppsFlyerAdobeConstants.AFEXTENSION;
import static com.appsflyer.adobeextension.AppsFlyerAdobeConstants.DEV_KEY_CONFIG;
import static com.appsflyer.adobeextension.AppsFlyerAdobeConstants.EVENT_SETTING_CONFIG;
import static com.appsflyer.adobeextension.AppsFlyerAdobeConstants.IS_DEBUG_CONFIG;
import static com.appsflyer.adobeextension.AppsFlyerAdobeConstants.TRACK_ATTR_DATA_CONFIG;
import static com.appsflyer.adobeextension.AppsFlyerAdobeConstants.WAIT_FOR_ECID;

public class AppsFlyerSharedStateListener extends ExtensionListener {

    public AppsFlyerSharedStateListener(final ExtensionApi extension, final String type, final String source) {
        super(extension, source, type);
    }

    @Override
    public void hear(final Event event) {
        ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
            @Override
            public void error(final ExtensionError extensionError) {
                Log.e(AFEXTENSION, "error receiving sharedState event: " + extensionError.getErrorName());
            }
        };

        Map<String, Object> eventData = event.getEventData();
        String stateOwner = (String) eventData.get("stateowner");

        if ("com.adobe.module.configuration".equals(stateOwner)) {
            Map<String, Object> configurationSharedState = getParentExtension().getApi().getSharedEventState("com.adobe.module.configuration", event, errorCallback);
            try {
                if (configurationSharedState != null) {
                    if (!configurationSharedState.isEmpty() && (configurationSharedState.get(DEV_KEY_CONFIG) != null)) {
                        String appsFlyerDevKey = configurationSharedState.get(DEV_KEY_CONFIG).toString();
                        String inAppEventSetting = null;
                        boolean isDebug = false;
                        boolean shouldTrackAttr = false;
                        boolean waitForECID = false;

                        if (configurationSharedState.get(IS_DEBUG_CONFIG) != null) {
                            isDebug = (boolean) configurationSharedState.get(IS_DEBUG_CONFIG);
                        }

                        if (configurationSharedState.get(TRACK_ATTR_DATA_CONFIG) != null) {
                            shouldTrackAttr = (boolean) configurationSharedState.get(TRACK_ATTR_DATA_CONFIG);
                        }

                        if (configurationSharedState.get(WAIT_FOR_ECID) != null) {
                            waitForECID = (boolean) configurationSharedState.get(WAIT_FOR_ECID);
                        }

                        if (configurationSharedState.get(EVENT_SETTING_CONFIG) != null) {
                            inAppEventSetting = configurationSharedState.get(EVENT_SETTING_CONFIG).toString();
                        }

                        getParentExtension().handleConfigurationEvent(appsFlyerDevKey, isDebug, shouldTrackAttr, inAppEventSetting, waitForECID);
                    } else {
                        Log.e(AFEXTENSION, "Cannot initialize AppsFlyer tracking without a valid DevKey");
                    }
                } else {
                    Log.e(AFEXTENSION, "Cannot initialize Appsflyer tracking: null configuration json");
                }
            } catch (NullPointerException npx) {
                Log.e(AFEXTENSION, "Exception while casting devKey to String: " + npx);
            }
        }
    }

    @Override
    protected AppsFlyerAdobeExtension getParentExtension() {
        return (AppsFlyerAdobeExtension) super.getParentExtension();
    }

    @Override
    public void onUnregistered() {
        super.onUnregistered();
    }
}