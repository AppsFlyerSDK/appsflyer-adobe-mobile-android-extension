package com.appsflyer.adobeextension;

import java.util.Map;

public interface AppsFlyerExtensionCallbacksListener {
    void onCallbackReceived(Map<String, String> callback);
    void onCallbackError(String errorMessage);
}
