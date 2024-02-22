<img src="./gitresources/AF Logo_primary logo.png" width="450" >

# appsflyer-adobe-mobile-android-extension

🛠 In order for us to provide optimal support, we would kindly ask you to submit any issues to support@appsflyer.com

> *When submitting an issue please specify your AppsFlyer sign-up (account) email , your app ID , production steps, logs, code snippets and any additional relevant information.*

## Table of content

- [Adding the SDK to your project](#add-sdk-to-project)
- [Initializing the SDK](#init-sdk)
- [Manual mode](#manual-mode)
- [Guides](#guides)
- [API](#api) 
- [Data Elements](#data-elements)
- [Initializing the SDK](#init-sdk)
- [Send consent for DMA compliance](#send-consent-for-DMA-compliance)


### <a id="plugin-build-for"> This plugin is built for
    
- Android AppsFlyer SDK **v6.13.0**

## <a id="add-sdk-to-project"> 📲 Adding the SDK to your project

Add the following to your app's `build.gradle (Module: app)` file:

```groovy
repositories {
    mavenCentral()
}

dependencies {
...
implementation 'com.appsflyer:appsflyer-adobe-sdk-extension:6.+'
implementation 'com.android.installreferrer:installreferrer:1.1'
}
```

> Add the installreferrer library to improve attribution accuracy, protects from install fraud and more.

## <a id="init-sdk"> 🚀 Initializing the SDK
    
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

In Addition to adding the init code, the settings inside the launch dashboard must be set.

<img src="./gitresources/LaunchAFInitNew.png" width="550" >

| Setting  | Description   |
| -------- | ------------- |
| AppsFlyer iOS App ID      | Your iTunes [application ID](https://support.appsflyer.com/hc/en-us/articles/207377436-Adding-a-new-app#available-in-the-app-store-google-play-store-windows-phone-store)  (required for iOS only)  |
| AppsFlyer Dev Key   | Your application [devKey](https://support.appsflyer.com/hc/en-us/articles/211719806-Global-app-settings-#sdk-dev-key) provided by AppsFlyer (required)  |
| Bind in-app events for    | Bind adobe event to appsflyer in-app events. For more info see the doc [here](/docs/Guides.md#events). |
| Send attribution data    | Send conversion data from the AppsFlyer SDK to adobe. This is required for data elements. |
| Debug Mode    | Debug mode - set to `true` for testing only.  |
| Wait for ECID   | Once enabled, the SDK Initialization will be delayed until the Experience Cloud ID is set.  |

> Note: For Send attribution data, use this feature if you are only working with ad networks that allow sharing user level data with 3rd party tools.


## <a id="manual-mode"> Manual mode
Starting version `6.13.0`, we support a manual mode to seperate the initialization of the AppsFlyer SDK and the start of the SDK.</br>
In this case, the AppsFlyer SDK won't start automatically, giving the developer more freedom when to start the AppsFlyer SDK.</br>
Please note that in manual mode, the developer is required to implement the API `AppsFlyerLib.getInstance().start(Activity activity)` in order to start the SDK.</br> 
You should set this mode before registering the `MobileCore` Extensions in `Application`.<br>
If you are using CMP to collect consent data this feature is needed. See explanation [here](#send-consent-for-DMA-compliance).
### Example:  
```java
AppsflyerAdobeExtension.manual = true;
``` 
Please look at the example below to see how to start SDK once you want.</br>
Keep in mind you shouldn't put the `start()` on a lifecycle method.</br>
To start the AppsFlyer SDK, use the `start()` API, like the following :  
```java
AppsFlyerLib.getInstance().start(this)
```   


## <a id="guides"> 📖 Guides

- [Deep Linking](/docs/Guides.md#deeplinking)
- [In-App Events](/docs/Guides.md#events)
- [Data Elements](/docs/Guides.md#data-elements)
- [Attribution Data tracking with Adobe Analytics](/docs/Guides.md#attr-data)
- [Deeplink Data tracking with Adobe Analytics](/docs/Guides.md#deeplink-data)
- [Adobe Analytics](/docs/AdobeAnalytics.md)

## <a id="api"> 📑 API
  
See the full [API](/docs/API.md) available for this plugin.


## <a id="data-elements"> 📂 Data Elements
  
Check out the available data elements [here](/docs/DataElements.md).


## <a id="send-consent-for-DMA-compliance"> Send consent for DMA compliance 
For a general introduction to DMA consent data, see [here](https://dev.appsflyer.com/hc/docs/send-consent-for-dma-compliance).<be> 
The SDK offers two alternative methods for gathering consent data:<br> 
- **Through a Consent Management Platform (CMP)**: If the app uses a CMP that complies with the [Transparency and Consent Framework (TCF) v2.2 protocol](https://iabeurope.eu/tcf-supporting-resources/), the SDK can automatically retrieve the consent details.<br> 
<br>OR<br><br> 
- **Through a dedicated SDK API**: Developers can pass Google's required consent data directly to the SDK using a specific API designed for this purpose. 
### Use CMP to collect consent data 
A CMP compatible with TCF v2.2 collects DMA consent data and stores it in <code>SharedPreferences</code>. To enable the SDK to access this data and include it with every event, follow these steps:<br> 
<ol> 
  <li> Call <code>AppsFlyerLib.getInstance().enableTCFDataCollection(true)</code> to instruct the SDK to collect the TCF data from the device. 
  <li> Set the the adapter to be manual : <code>AppsflyerAdobeExtension.manual = true</code>. <br> This will allow us to delay the Conversion call in order to provide the SDK with the user consent. 
  <li> Initialize Adobe. 
  <li> In the <code>Activity</code> class, use the CMP to decide if you need the consent dialog in the current session.
  <li> If needed, show the consent dialog, using the CMP, to capture the user consent decision. Otherwise, go to step 6. 
  <li> Get confirmation from the CMP that the user has made their consent decision, and the data is available in <code>SharedPreferences</code>.
  <li> Call <code>AppsFlyerLib.getInstance().start(this)</code>
</ol> 
 
 #### Application class
``` java
override fun onCreate() {
    super.onCreate()
    AppsFlyerLib.getInstance().enableTCFDataCollection(true)
    AppsflyerAdobeExtension.manual = true
    MobileCore.setApplication(this);
    MobileCore.setLogLevel(LoggingMode.DEBUG);
    try {
        AppsFlyerAdobeExtension.registerExtension();
        Analytics.registerExtension();
        Identity.registerExtension();
        Lifecycle.registerExtension();
        Signal.registerExtension();
        UserProfile.registerExtension();
        MobileCore.start(new AdobeCallback() {
            @Override
            public void call(Object o) {
                MobileCore.configureWithAppID("Dev-Key");
            }
        });

        AppsFlyerAdobeExtension.registerAppsFlyerExtensionCallbacks(new AppsFlyerExtensionCallbacksListener() {
          ...
        });

    } catch (Exception ex) {
        Log.d("AdobeException: ",ex.toString());
    }
}
```

 
### Manually collect consent data 
If your app does not use a CMP compatible with TCF v2.2, use the SDK API detailed below to provide the consent data directly to the SDK. 
<ol> 
  <li> Initialize <code>AppsflyerAdobeExtension</code> using manual mode and also <code>MobileCore</code>. This will allow us to delay the Conversion call in order to provide the SDK with the user consent. 
  <li> In the <code>Activity</code> class, determine whether the GDPR applies or not to the user.<br> 
  - If GDPR applies to the user, perform the following:  
      <ol> 
        <li> Given that GDPR is applicable to the user, determine whether the consent data is already stored for this session. 
            <ol> 
              <li> If there is no consent data stored, show the consent dialog to capture the user consent decision. 
              <li> If there is consent data stored continue to the next step. 
            </ol> 
        <li> To transfer the consent data to the SDK create an object called <code>AppsFlyerConsent</code> using the <code>forGDPRUser()</code> method with the following parameters:<br> 
          - <code>hasConsentForDataUsage</code> - Indicates whether the user has consented to use their data for advertising purposes.<br>
          - <code>hasConsentForAdsPersonalization</code> - Indicates whether the user has consented to use their data for personalized advertising purposes.
        <li> Call <code>AppsFlyerLib.getInstance().setConsentData()</code> with the <code>AppsFlyerConsent</code> object.    
        <li> Call <code>AppsFlyerLib.getInstance().start(this)</code>. 
      </ol><br> 
    - If GDPR doesn’t apply to the user perform the following: 
      <ol> 
        <li> Create an <code>AppsFlyerConsent</code> object using the <code>forNonGDPRUser()</code> method. This method doesn’t accept any parameters.
        <li> Call <code>AppsFlyerLib.getInstance().setConsentData()</code> with the <code>AppsFlyerConsent</code> object.  
        <li> Call <code>AppsFlyerLib.getInstance().start(this)</code>. 
      </ol> 
</ol> 

