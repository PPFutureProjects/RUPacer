package com.maomao.foldmenudemo;

import com.parse.Parse;


public class MyApplication extends android.app.Application {

    private static final String PARSE_APPLICATION_ID = "6XoDR3RpvpUXljoY1oitG8HBvRSOgL4oxxtEMxrp";
    private static final String PARSE_CLIENT_KEY = "QY0XA5buE93SyZSmmRBaVkgk0s5V1M23PZa6CTd9";

    @Override
    public void onCreate() {
        super.onCreate();

        //This will only be called once in your app's entire lifecycle.
        Parse.initialize(this, PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);

    }
}
