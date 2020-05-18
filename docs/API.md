# API

<img src="https://massets.appsflyer.com/wp-content/uploads/2018/06/20092440/static-ziv_1TP.png"  width="400" >


- [registerExtension](#registerExtension)
- [registerAppsFlyerExtensionCallbacks](#registerAppsFlyerExtensionCallbacks)


---

 ##### <a id="registerExtension"> **`static void registerExtension()`**

Register the AppsFlyer-Adobe Extension


*Example:*

```java
public class MainApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MobileCore.setApplication(this);
        MobileCore.setLogLevel(LoggingMode.DEBUG);
        try {
        ...
            AppsFlyerAdobeExtension.registerExtension();
        ...
        } catch (Exception e){

        }
    }
}

```

---


 ##### <a id="registerAppsFlyerExtensionCallbacks"> **`static void registerAppsFlyerExtensionCallbacks(AppsFlyerExtensionCallbacksListener cl)`**
 

| parameter          | type                        | description  |
| -----------        |-----------------------------|--------------|
| `callbacksListener` | `AppsFlyerExtensionCallbacksListener` | AppsFlyer Deeplink interface|

*Example:*

```java
public class MainApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MobileCore.setApplication(this);
        MobileCore.setLogLevel(LoggingMode.DEBUG);
        try {
            MobileServices.registerExtension();
            Analytics.registerExtension();
            Griffon.registerExtension();
            AppsFlyerAdobeExtension.registerExtension();
            Identity.registerExtension();
            Lifecycle.registerExtension();
            Signal.registerExtension();
            UserProfile.registerExtension();
            MobileCore.start(new AdobeCallback () {
                @Override
                public void call(Object o) {
                    MobileCore.configureWithAppID("launch-key");
                }
            });
        } catch (Exception e){

        }
        
        AppsFlyerAdobeExtension.registerAppsFlyerExtensionCallbacks(new AppsFlyerExtensionCallbacksListener() {
            @Override
            public void onCallbackReceived(Map<String, String> callback) {
                if(callback.get("callback_type").equals("onConversionDataReceived")){
                    if(callback.get("is_first_launch").equals("true")){
                        // deferred deeplink
                    }
                } else if(callback.get("callback_type").equals("onAppOpenAttribution")){
                    // direct deeplink
                }
                Log.d("TAG", callback.toString());
            }

            @Override
            public void onCallbackError(String errorMessage) {
                Log.d("TAG", errorMessage);
            }
        });
    }
}

```

