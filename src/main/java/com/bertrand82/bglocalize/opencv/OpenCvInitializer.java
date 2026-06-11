package com.bertrand82.bglocalize.opencv;

import java.util.concurrent.atomic.AtomicBoolean;

public final class OpenCvInitializer {

    public static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    private OpenCvInitializer() {
    }

    public static void initialize() {
    	
        if (INITIALIZED.compareAndSet(false, true)) {
            nu.pattern.OpenCV.loadLocally();
            System.out.println("initialized opencv :"+INITIALIZED);
        }
    }
}
