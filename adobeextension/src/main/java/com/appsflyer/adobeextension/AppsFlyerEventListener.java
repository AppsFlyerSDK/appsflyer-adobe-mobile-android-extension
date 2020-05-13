package com.appsflyer.adobeextension;

import android.util.Log;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.ExtensionListener;
import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AppsFlyerLib;

import java.util.HashMap;
import java.util.Map;

import static com.appsflyer.adobeextension.AppsFlyerAdobeConstants.AFEXTENSION;
import static com.appsflyer.adobeextension.AppsFlyerAdobeConstants.APPSFLYER_ATTRIBUTION_DATA;
import static com.appsflyer.adobeextension.AppsFlyerAdobeExtension.eventSetting;

public class AppsFlyerEventListener extends ExtensionListener {

    // trackAction() || trackState()
    private static final String ACTION = "action";
    private static final String STATE = "state";

    /* Event types from adobe dashboard */
    private static final String TRACK_ALL_EVENTS = "all";
    private static final String TRACK_ACTION_EVENTS = "actions";
    private static final String TRACK_STATE_EVENTS = "states";
    private static final String TRACK_NO_EVENTS = "none";

    protected AppsFlyerEventListener(ExtensionApi extension, String type, String source) {
        super(extension, type, source);
    }

    @Override
    public void hear(Event event) {

        boolean trackActionEvent = eventSetting.equals(TRACK_ALL_EVENTS) || eventSetting.equals(TRACK_ACTION_EVENTS);
        boolean trackStateEvent = eventSetting.equals(TRACK_ALL_EVENTS) || eventSetting.equals(TRACK_STATE_EVENTS);

        if (eventSetting.equals(TRACK_NO_EVENTS)) {
            return;
        }

        if (event.getType().equals("com.adobe.eventtype.generic.track") && event.getSource().equals("com.adobe.eventsource.requestcontent")) {
            Map<String,Object> eventData = event.getEventData();
            Object nestedData = eventData.get("contextdata");
            Object actionEventName = eventData.get(ACTION);
            Object stateEventName = eventData.get(STATE);
            boolean is_action_event = actionEventName != null;
            boolean is_state_event = stateEventName != null;

            if (trackActionEvent && is_action_event) {
                // Discard if event is "AppsFlyer Attribution Data" event.
                if (actionEventName.equals(APPSFLYER_ATTRIBUTION_DATA)) {
                    Log.d(AFEXTENSION, "Discarding event binding for AppsFlyer Attribution Data event");
                    return;
                }

                if (AppsFlyerAdobeExtension.af_application != null) {
                    AppsFlyerLib.getInstance().trackEvent(AppsFlyerAdobeExtension.af_application, actionEventName.toString(), getAppsFlyerEventMap(nestedData));
                } else {
                    Log.e(AFEXTENSION, "Application is null, please set Application using AppsFlyerAdobeExtension.setApplication(this);");
                }
            } else if(trackStateEvent && is_state_event){
                AppsFlyerLib.getInstance().trackEvent(AppsFlyerAdobeExtension.af_application, stateEventName.toString(), getAppsFlyerEventMap(nestedData));
            }
        }
    }

    /**
     * Convert eventData.get("contextdata") to AF event Map<String, Object>
     * @param nestedData eventData.get("contextdata")
     * @return map - Map<String, Object>
     */
    private static Map<String, Object> getAppsFlyerEventMap(Object nestedData){
        Map<String,Object> map = new HashMap<>();
        if (nestedData != null) {
            try {
                map = (Map<String, Object>) nestedData;
                String revenue = (String)map.get("revenue");
                if (revenue != null) {
                    map.put(AFInAppEventParameterName.REVENUE, revenue);
                    String currency = (String)map.get("currency");
                    if (currency != null) {
                        map.put(AFInAppEventParameterName.CURRENCY, currency);

                    }
                }
            } catch (Exception ex) {
                Log.e(AFEXTENSION,"Error casting contextdata: " + ex.toString());
            }
        }
        return map;
    }

}