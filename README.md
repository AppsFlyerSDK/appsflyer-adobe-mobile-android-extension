




# AppsFlyer Android SDK Extension for Adobe Mobile SDK

## Table of content
- [Installation](#installation)
- [Event Tracking](#eventTracking)
- [Extension Callbacks](#callbacks)


## <a id="installation">  Installation

***Important***:
When using the AppsFlyer Adobe Extension for Android the `Application Context` must be set using the `setApplication(Application)` API.  for example:
```java
@Override  
public void onCreate() {  
    super.onCreate();  
  
  MobileCore.setApplication(this);  
  ...
   try {  
        AppsFlyerAdobeExtension.setApplication(this);  
		AppsFlyerAdobeExtension.registerExtension();
        ...
    }
}
```


For instructions on using AppsFlyer's Adobe Mobile SDK Extension please see: https://aep-sdks.gitbook.io/docs/getting-started/create-a-mobile-property

After adding the extension to the mobile property, Dev Key field and save the extension settings. 
(Android) App ID is automatically set to the Android Bundle identifier.
![AppsFlyerAdobeSDK](https://github.com/AppsFlyerSDK/AppsFlyerAdobeExtension/blob/master/gitresources/img.png)


For more information on adding applications to the AppsFlyer dashboard see [here](https://support.appsflyer.com/hc/en-us/articles/207377436-Adding-a-New-App-to-the-AppsFlyer-Dashboard)

Information on adding the extension to your Android Studio Project is available on the Launch dashboard.

## <a id="eventTracking"> Event Tracking
All events that are invoked using the `MobileCore.trackAction(String action, Map<String,String> contextData)` API are automatically tracked to the AppsFlyer Platform as in-app events; For example, calling this API:
```java
final Map<String,String> eventMap = new HashMap<>();  
eventMap.put("currency", "USD");  
eventMap.put("revenue", "200");  
eventMap.put("freehand", "param");

MobileCore.trackAction("testAnalyticsAction", eventMap);
```
will result in a `testAnalyticsAction` event tracked on the AppsFlyer Dashboard with a revenue of 200USD.

 `revenue` and `currency` parameters are mapped to `af_revenue` and `af_currency`.

## <a id="callbacks"> Extension Callbacks
 Registering for deferred deep link and deep link callbacks:
```java
AppsFlyerAdobeExtension.registerAppsFlyerExtensionCallbacks(new AppsFlyerExtensionCallbacksListener() {  
  @Override  
  public void onCallbackReceived(Map<String, String> callback) {  
        Log.d("TAG", callback.toString());  
  }  
  
  @Override  
  public void onCallbackError(String errorMessage) {  
	  Log.d("TAG", errorMessage);
    }  
});
``` 
The returned map should contain a `callback_type` key to distinguish between `onConversionDataReceived` (deferred deep link) and `onAppOpenAttribution`  (deep link).
