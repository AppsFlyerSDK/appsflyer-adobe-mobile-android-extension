package com.appsflyer.adobeextension;

import static com.appsflyer.adobeextension.AppsFlyerAdobeConstants.AFEXTENSION;
import static com.appsflyer.adobeextension.AppsFlyerAdobeConstants.APPSFLYER_ATTRIBUTION_DATA;
import static com.appsflyer.adobeextension.AppsFlyerAdobeConstants.APPSFLYER_ENGAGMENT_DATA;
import static com.appsflyer.adobeextension.AppsFlyerAdobeConstants.APPSFLYER_ID;
import static com.appsflyer.adobeextension.AppsFlyerAdobeConstants.CALLBACK_TYPE;
import static com.appsflyer.adobeextension.AppsFlyerAdobeConstants.IS_FIRST_LAUNCH;
import static com.appsflyer.adobeextension.AppsFlyerAdobeConstants.MEDIA_SOURCE;
import static com.appsflyer.adobeextension.AppsFlyerAdobeConstants.SDK_VERSION;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
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
import com.appsflyer.internal.platform_extension.Plugin;
import com.appsflyer.internal.platform_extension.PluginInfo;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppsFlyerAdobeExtension extends Extension {
    public static Boolean manual = false;
    public static String eventSetting = "";
    static Application af_application;
    static WeakReference<Activity> af_activity;
    private static boolean didReceiveConfigurations;
    private static boolean trackAttributionData = false;
    private static AppsFlyerExtensionCallbacksListener afCallbackListener = null;
    private static Map<String, Object> gcd;
    private final Object executorMutex = new Object();
    private boolean sdkStared = false;
    private ExecutorService executor;
    private String ecid;

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
        registerCallbacks();
    }

    //   Deprecated API - replaced by  MobileCore.getApplication();
    @Deprecated
    public static void setApplication(Application application) {
        if (application == null) {
            Log.e(AFEXTENSION, "Cannot set null application");
        } else {
            af_application = application;
            registerCallbacks();
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


    public static void registerCallbacks() {
        Application.ActivityLifecycleCallbacks callbacks = new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                af_activity = new WeakReference<Activity>(activity);

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        };

        af_application.registerActivityLifecycleCallbacks(callbacks);
    }

    public static void registerAppsFlyerExtensionCallbacks(AppsFlyerExtensionCallbacksListener
                                                                   callbacksListener) {
        if (callbacksListener != null) {
            afCallbackListener = callbacksListener;
        } else {
            Log.e(AFEXTENSION, "Cannot register callbacks listener with null object");
        }
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

    public static Map<String, Object> getConversionData() {
        return gcd;
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

    void handleConfigurationEvent(final String appsFlyerDevKey,
                                  final boolean appsFlyerIsDebug,
                                  final boolean trackAttrData,
                                  final String inAppEventSetting, final boolean waitForECID) {
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (af_application != null && !didReceiveConfigurations) {
                    Map<String, String> additionalParams = new HashMap<String, String>();
                    additionalParams.put("build_number", String.valueOf(BuildConfig.VERSION_CODE));
                    PluginInfo pluginInfo = new PluginInfo(Plugin.ADOBE_MOBILE,
                            BuildConfig.VERSION_NAME, additionalParams);
                    AppsFlyerLib.getInstance().setPluginInfo(pluginInfo);
                    // Set Adobe ID as the AppsFlyer customerUserId as early as possible.
                    AppsFlyerLib.getInstance().setDebugLog(appsFlyerIsDebug);
                    AppsFlyerLib.getInstance().init(
                            appsFlyerDevKey,
                            getConversionListener(),
                            af_application.getApplicationContext());
                    // wait for Adobe ID to be sent with the install event
                    if (waitForECID) {
                        afLogger("waiting for Experience Cloud Id");
                        AppsFlyerLib.getInstance().waitForCustomerUserId(true);
                    }

                    Identity.getExperienceCloudId(new AdobeCallback<String>() {
                        @Override
                        public void call(String s) {
                            if (s != null) {
                                ecid = s;
                            }
                            String id = (ecid != null ? ecid : "");
                            if (waitForECID && sdkStared) {
                                Context context = (af_activity != null && af_activity.get() != null)
                                        ? af_activity.get()
                                        : af_application.getApplicationContext();
                                AppsFlyerLib.getInstance().setCustomerIdAndLogSession(id, context);
                            } else {
                                AppsFlyerLib.getInstance().setCustomerUserId(id);
                            }

                        }
                    });


                    startSDK();

                    trackAttributionData = trackAttrData;
                    eventSetting = inAppEventSetting;
                    didReceiveConfigurations = true;

                } else if (af_application == null) {
                    Log.e(AFEXTENSION, "Null application context error");
                }
            }
        });
    }

    private void startSDK() {
        if (sdkStared || AppsFlyerAdobeExtension.manual) {
            return;
        }
        if (af_activity != null && af_activity.get() != null) {
            afLogger("start with Activity context");
            AppsFlyerLib.getInstance().start(af_activity.get());
            sdkStared = true;
        } else if (af_application != null) {
            afLogger("start with Application context");
            AppsFlyerLib.getInstance().start(af_application.getApplicationContext());
            sdkStared = true;
        } else {
            afLogger("no context to start the SDK");
        }
    }

    private AppsFlyerConversionListener getConversionListener() {
        return new AppsFlyerConversionListener() {
            @Override
            public void onConversionDataSuccess(Map<String, Object> conversionData) {
                afLogger("called onConversionDataSuccess");
                conversionData.put(CALLBACK_TYPE, "onConversionDataReceived");
                if (trackAttributionData) {
                    boolean isFirstLaunch = (Boolean) conversionData.get(IS_FIRST_LAUNCH);
                    if (isFirstLaunch) {
                        getApi().setSharedEventState(getSaredEventState(conversionData), null, null);
                        // add appsflyer_id to send to MobileCore
                        if (af_application != null) {
                            conversionData.put(APPSFLYER_ID, AppsFlyerLib.getInstance().getAppsFlyerUID(af_application.getApplicationContext()));
                        }
                        // Send AppsFlyer Attribution data to Adobe Analytics;

                        if (ecid != null) {
                            conversionData.put("ecid", ecid);
                        }

                        MobileCore.trackAction(APPSFLYER_ATTRIBUTION_DATA, setKeyPrefix(conversionData));


                    } else {
                        Log.d(AFEXTENSION, "Skipping attribution data reporting, not first launch");
                    }
                }

                if (afCallbackListener != null) {
                    afCallbackListener.onCallbackReceived(convertConversionData(conversionData));
                }

                gcd = conversionData;
            }

            @Override
            public void onConversionDataFail(String errorMessage) {
                afLogger("called onConversionDataFail");

                if (afCallbackListener != null) {
                    afCallbackListener.onCallbackError(errorMessage);
                }
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> deepLinkData) {
                afLogger("called onAppOpenAttribution");
                deepLinkData.put(CALLBACK_TYPE, "onAppOpenAttribution");
                if (ecid != null) {
                    deepLinkData.put("ecid", ecid);
                }
                MobileCore.trackAction(APPSFLYER_ENGAGMENT_DATA, setKeyPrefixOnAppOpenAttribution(deepLinkData));

                if (afCallbackListener != null) {
                    afCallbackListener.onCallbackReceived(deepLinkData);
                }
            }

            @Override
            public void onAttributionFailure(String errorMessage) {
                afLogger("called onAttributionFailure");

                if (afCallbackListener != null) {
                    afCallbackListener.onCallbackError(errorMessage);
                }

            }
        };
    }

    private Map<String, String> setKeyPrefix(Map<String, Object> attributionParams) {
        Map<String, String> newConversionMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : attributionParams.entrySet()) {
            if (!entry.getKey().equals(CALLBACK_TYPE)) {
                if (entry.getValue() != null) {
                    String newKey = "appsflyer." + entry.getKey();
                    newConversionMap.put(newKey, entry.getValue().toString());
                } else {
                    String newKey = "appsflyer." + entry.getKey();
                    newConversionMap.put(newKey, null);
                }
            }
        }
        return newConversionMap;
    }

    private Map<String, String> setKeyPrefixOnAppOpenAttribution
            (Map<String, String> attributionParams) {
        Map<String, String> newConversionMap = new HashMap<>();
        for (Map.Entry<String, String> entry : attributionParams.entrySet()) {
            if (!entry.getKey().equals(CALLBACK_TYPE)) {
                String newKey = "appsflyer.af_engagement_" + entry.getKey();
                newConversionMap.put(newKey, entry.getValue());
            }
        }
        return newConversionMap;
    }

    private Map<String, Object> getSaredEventState(Map<String, Object> conversionData) {
        // copy conversion data
        Map<String, Object> sharedEventState = new HashMap<>(conversionData);

        sharedEventState.put(APPSFLYER_ID, AppsFlyerLib.getInstance().getAppsFlyerUID(af_application));
        sharedEventState.put(SDK_VERSION, AppsFlyerLib.getInstance().getSdkVersion());
        if (!conversionData.containsKey(MEDIA_SOURCE)) {
            sharedEventState.put(MEDIA_SOURCE, "organic");
        }
        sharedEventState.remove(IS_FIRST_LAUNCH);
        sharedEventState.remove(CALLBACK_TYPE);

        return sharedEventState;
    }

    private ExecutorService getExecutor() {
        synchronized (executorMutex) {
            if (executor == null) {
                executor = Executors.newSingleThreadExecutor();
            }
            return executor;
        }
    }

    private void afLogger(String msg) {
        Log.d("AppsFlyer_adobe_ext", msg);
    }


}