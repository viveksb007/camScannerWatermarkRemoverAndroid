package com.scanner.watermarkremover;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

import java.lang.reflect.Method;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
