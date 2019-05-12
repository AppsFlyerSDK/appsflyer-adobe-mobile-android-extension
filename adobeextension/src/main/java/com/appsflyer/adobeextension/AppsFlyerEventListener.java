package com.appsflyer.adobeextension;

import android.util.Log;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.ExtensionListener;
import com.appsflyer.AppsFlyerLib;

import java.util.HashMap;
import java.util.Map;

import static com.appsflyer.adobeextension.AppsFlyerAdobeExtension.AFEXTENSION;

public class AppsFlyerEventListener extends ExtensionListener {
    protected AppsFlyerEventListener(ExtensionApi extension, String type, String source) {
        super(extension, type, source);
    }

    @Override
    public void hear(Event event) {
        if (event.getType().equals("com.adobe.eventtype.generic.track") && event.getSource().equals("com.adobe.eventsource.requestcontent")) {
            Map<String,Object> eventData = event.getEventData();
            Object nestedData = eventData.get("contextdata");
            Object eventName = eventData.get("action");
            Map<String,Object> valuesMap = new HashMap<>();

            if (eventName != null) {
                // Discard if event is "AppsFlyer Attribution Data" event.
                if (eventName.equals(AppsFlyerAdobeExtension.APPSFLYER_ATTRIBUTION_DATA)) {
                    Log.d(AFEXTENSION, "Discarding event binding for AppsFlyer Attribution Data event");
                    return;
                }

                if (nestedData != null) {
                    try {
                        valuesMap = (Map<String, Object>) nestedData;
                        String revenue = (String)valuesMap.get("revenue");
                        if (revenue != null) {
                            valuesMap.put("af_revenue", revenue);
                            String currency = (String)valuesMap.get("currency");
                            if (currency != null) {
                                valuesMap.put("af_currency", currency);
                            }
                        }
                    } catch (Exception ex) {
                        Log.e(AFEXTENSION,"Error casting contextdata: "+ex.toString());
                    }
                }
                if (AppsFlyerAdobeExtension.af_application != null) {
                    AppsFlyerLib.getInstance().trackEvent(AppsFlyerAdobeExtension.af_application, eventName.toString(), valuesMap);
                } else {
                    Log.e(AFEXTENSION, "Application is null, please set Application using AppsFlyerAdobeExtension.setApplication(this);");
                }
            }
        }
    }
}