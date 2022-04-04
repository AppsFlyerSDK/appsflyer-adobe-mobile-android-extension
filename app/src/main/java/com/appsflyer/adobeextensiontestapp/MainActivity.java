package com.appsflyer.adobeextensiontestapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.adobe.marketing.mobile.MobileCore;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

   private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button =  findViewById(R.id.evtButton);

        final Map<String,String> evtMap = new HashMap<>();
        evtMap.put("currency", "ILS");
        evtMap.put("revenue", "200");
        evtMap.put("freehand", "param");


        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                MobileCore.trackAction("testTrackAction", evtMap);
            }
        });
    }
}
