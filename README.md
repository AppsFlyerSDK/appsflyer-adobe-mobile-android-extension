
# AppsFlyer Android SDK Extension for Adobe Mobile SDK

## Table of content
- [Installation](#installation)
- [Extension Initialisation](#Initialisation)
- [Event Tracking](#eventTracking)
- [Extension Callbacks](#callbacks)
- [Attribution Data tracking with Adobe Analytics](#analyticsPostback)


## <a id="installation">  Installation
Import the latest `appsflyer-adobe-sdk-extension` in your build.gradle file:
```groovy
dependencies {
  ...
  implementation 'com.appsflyer:appsflyer-adobe-sdk-extension:1.2
}
``` 
If missing, add Maven Central Repository to the repositories struct:
```groovy
repositories {  
    mavenCentral()  
}
``` 
## <a id="Initialisation"> Extension Initialisation: 
Register the AppsFlyer extension from your `Application` class, alongside the Adobe SDK initialisation code: 
```java
@Override  
public void onCreate() {  
  super.onCreate();  
  
  MobileCore.setApplication(this);  
  MobileCore.setLogLevel(LoggingMode.DEBUG);  
  ...
  try {
  ...
  AppsFlyerAdobeExtension.registerExtension();
  ...
  }
}
```


For Additional instructions on using AppsFlyer's Adobe Mobile SDK Extension please see: https://aep-sdks.gitbook.io/docs/getting-started/create-a-mobile-property

After adding the extension to the mobile property, Dev Key field and save the extension settings. 
(Android) App ID is automatically set to the Android Bundle identifier.
![AppsFlyerAdobeSDK](https://github.com/AppsFlyerSDK/AppsFlyerAdobeExtension/blob/master/gitresources/img.png)

For more information on adding applications to the AppsFlyer dashboard see [here](https://support.appsflyer.com/hc/en-us/articles/207377436-Adding-a-New-App-to-the-AppsFlyer-Dashboard)

Information on adding the extension to your Android Studio Project is available on the Launch dashboard.

***Important***: the `setApplication(Application)` API was ***deprecated*** as it is no longer required.

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

## <a id="analyticsPostback"> Attribution Data tracking with Adobe Analytics
Checking the "Send attribution data to Adobe Analytics" toggle on the [Configurations Dashboard](#Initialisation) will automatically send AppsFlyer Attribution data to Adobe Analytics using the `MobileCore.trackAction()` API - The data will be sent as an "*AppsFlyer Attribution Data*" Action.

All ContextData will be prefixed by the `appsflyer.` prefix, for example: `appsflyer.campaign`, `appsflyer.adset` etc.

*The Adobe Analytics Extension must be added and configured in the client Application.*

**NOTE**: Use this feature if you are only working with ad networks that allow sharing user level data with 3rd party tools.