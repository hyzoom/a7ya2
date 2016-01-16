package com.ishraq.a7ya2;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;

/**
 * Created by hp on 16/12/2015.
 */
public class ParseApp extends Application {
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "0MMB9K27EjRFDQgHUDHpLb6T28P5c624WFuGwwF1", "nb41vxcgT495TW5EQuhBlC5RifyAedErKrhsd7LO");
        ParseInstallation.getCurrentInstallation().saveInBackground();
        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
    }
}
