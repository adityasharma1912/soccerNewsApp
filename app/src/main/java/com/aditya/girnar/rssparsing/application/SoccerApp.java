package com.aditya.girnar.rssparsing.application;

import android.app.Application;
import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by Aditya on 2/19/2016.
 */
public class SoccerApp extends Application {

    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = SoccerApp.this;
        Fresco.initialize(mContext);
    }
}
