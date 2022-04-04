<img src="./gitresources/AF Logo_primary logo.png" width="450" >

# appsflyer-adobe-mobile-android-extension

🛠 In order for us to provide optimal support, we would kindly ask you to submit any issues to support@appsflyer.com

> *When submitting an issue please specify your AppsFlyer sign-up (account) email , your app ID , production steps, logs, code snippets and any additional relevant information.*

## Table of content

- [Adding the SDK to your project](#add-sdk-to-project)
- [Initializing the SDK](#init-sdk)
- [Guides](#guides)
- [API](#api) 
- [Data Elements](#data-elements)


### <a id="plugin-build-for"> This plugin is built for
    
- Android AppsFlyer SDK **v6.5.4**

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
