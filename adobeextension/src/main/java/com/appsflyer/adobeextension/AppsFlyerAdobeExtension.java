package com.appsflyer.adobeextension;


import android.app.Application;

import android.util.Log;
import com.adobe.marketing.mobile.*;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;


public class AppsFlyerAdobeExtension extends Extension {
    private final Object executorMutex = new Object();
    private ExecutorService executor;
    private static boolean didReceiveConfigurations;
    private static AppsFlyerExtensionCallbacksListener afCallbackListener = null;
    static Application af_application;

    static final String AFEXTENSION = "AppsFlyerAdobeExtension";

    public AppsFlyerAdobeExtension(final ExtensionApi extensionApi) {
        super(extensionApi);
        ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
            @Override
            public void error(final ExtensionError extensionError) {
                Log.e(AFEXTENSION, "Error registering listener: "+ extensionError.getErrorName());
            }
        };

        // SharedState listener for configuration JSON
        getApi().registerEventListener("com.adobe.eventType.hub", "com.adobe.eventSource.sharedState", AppsFlyerSharedStateListener.class, errorCallback);

        // Event Binding for generic track events
        getApi().registerEventListener("com.adobe.eventType.generic.track","com.adobe.eventSource.requestContent", AppsFlyerEventListener.class, errorCallback);
    }

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
                Log.e(AFEXTENSION, "Error registering extension: "+ extensionError.getErrorName());
            }
        };

        if (!MobileCore.registerExtension(AppsFlyerAdobeExtension.class, errorCallback)) {
            Log.e(AFEXTENSION, "Failed to register"+AFEXTENSION+"extension");
        }
    }

    @Override
    public String getName()  {
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

    void handleConfigurationEvent(final String appsFlyerDevKey, final boolean appsFlyerIsDebug) {
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
                    AppsFlyerLib.getInstance().setExtension("adobe_extension");
                    AppsFlyerLib.getInstance().init(appsFlyerDevKey, getConversionListener(), af_application.getApplicationContext());
                    AppsFlyerLib.getInstance().trackAppLaunch(af_application.getApplicationContext(), appsFlyerDevKey);
                    AppsFlyerLib.getInstance().startTracking(af_application);
                    didReceiveConfigurations = true;
                } else if (af_application == null) {
                    Log.e(AFEXTENSION, "Cannot initialize AppsFlyer tracking without setting AppsFlyerAdobeExtension.setApplication(Application)");
                } else {
                    Log.e(AFEXTENSION, "Cannot initialize AppsFlyer tracking: invalid configuration json");
                }
            }
        });
    }

    //todo make callback_types constants
    private AppsFlyerConversionListener getConversionListener() {
        return new AppsFlyerConversionListener() {
            @Override
            public void onInstallConversionDataLoaded(Map<String, String> conversionData) {
                conversionData.put("callback_type", "onConversionDataReceived");
                afCallbackListener.onCallbackReceived(conversionData);
            }

            @Override
            public void onInstallConversionFailure(String errorMessage) {
                afCallbackListener.onCallbackError(errorMessage);
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> deepLinkData) {
                deepLinkData.put("callback_type", "onAppOpenAttribution");
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

    private  ExecutorService getExecutor() {
        synchronized (executorMutex) {
            if (executor == null) {
                executor = Executors.newSingleThreadExecutor();
            }
            return executor;
        }
    }
}