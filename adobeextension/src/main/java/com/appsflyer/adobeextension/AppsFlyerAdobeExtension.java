package com.appsflyer.adobeextension;

import android.app.Application;
import android.util.Log;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.adobe.marketing.mobile.ExtensionUnexpectedError;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.MobileCore;
import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppsFlyerAdobeExtension extends Extension {
    private final Object executorMutex = new Object();
    private ExecutorService executor;
    private static boolean didReceiveConfigurations;
    private static boolean trackAttributionData = false;
    public static String eventSetting = null;
    private static AppsFlyerExtensionCallbacksListener afCallbackListener = null;
    static Application af_application;
    private static final String CALLBACK_TYPE = "callback_type";
    private static final String APPSFLYER_ID = "appsflyer_id";
    private static final String IS_FIRST_LAUNCH = "is_first_launch";
    static final String APPSFLYER_ATTRIBUTION_DATA = "AppsFlyer Attribution Data";
    static final String AFEXTENSION = "AppsFlyerAdobeExtension";

    public AppsFlyerAdobeExtension(final ExtensionApi extensionApi) {
        super(extensionApi);
        ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
            @Override
            public void error(final ExtensionError extensionError) {
                Log.e(AFEXTENSION, "Error registering listener: " + extensionError.getErrorName());
            }
        };

        // SharedState listener for configuration JSON
        getApi().registerEventListener("com.adobe.eventType.hub", "com.adobe.eventSource.sharedState", AppsFlyerSharedStateListener.class, errorCallback);

        // Event Binding for generic track events
        getApi().registerEventListener("com.adobe.eventType.generic.track", "com.adobe.eventSource.requestContent", AppsFlyerEventListener.class, errorCallback);

        af_application = MobileCore.getApplication();
    }

    //   Deprecated API - replaced by  MobileCore.getApplication();
    @Deprecated
    public static void setApplication(Application application) {
        if (application == null) {
            Log.e(AFEXTENSION, "Cannot set null application");
        } else {
            af_application = application;
        }
    }

    public static void registerExtension() {
        ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
            @Override
            public void error(final ExtensionError extensionError) {
                Log.e(AFEXTENSION, "Error registering extension: " + extensionError.getErrorName());
            }
        };

        if (!MobileCore.registerExtension(AppsFlyerAdobeExtension.class, errorCallback)) {
            Log.e(AFEXTENSION, "Failed to register" + AFEXTENSION + "extension");
        }
    }

    @Override
    public String getName() {
        return "com.appsflyer.adobeextension";
    }

    @Override
    public void onUnregistered() {
    }

    @Override
    protected void onUnexpectedError(ExtensionUnexpectedError extensionUnexpectedError) {
        super.onUnexpectedError(extensionUnexpectedError);
    }

    @Override
    protected String getVersion() {
        return super.getVersion();
    }

    void handleConfigurationEvent(final String appsFlyerDevKey, final boolean appsFlyerIsDebug, final boolean trackAttrData, final String inAppEventSetting) {
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (af_application != null && !didReceiveConfigurations) {
                    // Set Adobe ID as the AppsFlyer customerUserId as early as possible.
                    Identity.getExperienceCloudId(new AdobeCallback<String>() {
                        @Override
                        public void call(String s) {
                            if (s != null) {
                                AppsFlyerLib.getInstance().setCustomerUserId(s);
                            }
                        }
                    });

                    AppsFlyerLib.getInstance().setDebugLog(appsFlyerIsDebug);
                    AppsFlyerLib.getInstance().init(appsFlyerDevKey, getConversionListener(), af_application.getApplicationContext());
                    AppsFlyerLib.getInstance().trackAppLaunch(af_application.getApplicationContext(), appsFlyerDevKey);
                    AppsFlyerLib.getInstance().startTracking(af_application);
                    trackAttributionData = trackAttrData;
                    eventSetting = inAppEventSetting;
                    didReceiveConfigurations = true;

                } else if (af_application == null) {
                    Log.e(AFEXTENSION, "Null application context error");
                } else {
                    Log.e(AFEXTENSION, "Configurations error");
                }
            }
        });
    }

    private AppsFlyerConversionListener getConversionListener() {
        return new AppsFlyerConversionListener() {
            @Override
            public void onConversionDataSuccess(Map<String, Object> conversionData) {
                conversionData.put(CALLBACK_TYPE, "onConversionDataReceived");
                if (trackAttributionData) {
                    boolean isFirstLaunch = (Boolean) conversionData.get(IS_FIRST_LAUNCH);
                    if (isFirstLaunch) {
                        // add appsflyer_id to send to MobileCore
                        conversionData.put(APPSFLYER_ID, AppsFlyerLib.getInstance().getAppsFlyerUID(af_application.getApplicationContext()));

                        // Send AppsFlyer Attribution data to Adobe Analytics;
                        MobileCore.trackAction(APPSFLYER_ATTRIBUTION_DATA, setKeyPrefix(conversionData));
                    } else {
                        Log.d(AFEXTENSION, "Skipping attribution data reporting, not first launch");
                    }
                }

                afCallbackListener.onCallbackReceived(convertConversionData(conversionData));
            }

            @Override
            public void onConversionDataFail(String errorMessage) {
                afCallbackListener.onCallbackError(errorMessage);
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> deepLinkData) {
                deepLinkData.put(CALLBACK_TYPE, "onAppOpenAttribution");
                afCallbackListener.onCallbackReceived(deepLinkData);
            }

            @Override
            public void onAttributionFailure(String errorMessage) {
                afCallbackListener.onCallbackError(errorMessage);
            }
        };
    }

    public static void registerAppsFlyerExtensionCallbacks(AppsFlyerExtensionCallbacksListener callbacksListener) {
        if (callbacksListener != null) {
            afCallbackListener = callbacksListener;
        } else {
            Log.e(AFEXTENSION, "Cannot register callbacks listener with null object");
        }
    }

    private Map<String, String> setKeyPrefix(Map<String, Object> attributionParams) {
        Map<String, String> newConversionMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : attributionParams.entrySet()) {
            if (!entry.getKey().equals(CALLBACK_TYPE)) {
                String newKey = "appsflyer." + entry.getKey();
                newConversionMap.put(newKey, entry.getValue().toString());
            }
        }
        return newConversionMap;
    }

    /**
     * Convert conversion data from Map<String,Object> to Map<String,String>
     *
     * @param map
     * @return
     */
    private static Map<String, String> convertConversionData(Map<String, Object> map) {
        Map<String, String> newMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                newMap.put(entry.getKey(), entry.getValue().toString());
            } else {
                newMap.put(entry.getKey(), null);
            }
        }

        return newMap;
    }

    private ExecutorService getExecutor() {
        synchronized (executorMutex) {
            if (executor == null) {
                executor = Executors.newSingleThreadExecutor();
            }
            return executor;
        }
    }
}