package com.linroid.rxshell;

import android.util.Log;

import java.io.File;

/**
 * @author linroid <linroid@gmail.com>
 * @since 06/03/2017
 */
/** package **/ class Utils {
    private static final String TAG = "RxShellUtils";

    static final String[] SU_PATHS = { "/data/bin/", "/system/bin/", "/system/xbin/", "/sbin/",
            "/data/local/xbin/", "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/",
            "/data/local/" };

    /**
     * Determine the path of the su executable.
     *
     * Code from https://github.com/miracle2k/android-autostarts, use under Apache License was
     * agreed by Michael Elsdörfer
     */
    public static String getSuPath() {
        for (String p : SU_PATHS) {
            File su = new File(p + "su");
            if (su.exists()) {
                Log.d(TAG, "su found at: " + p);
                return su.getAbsolutePath();
            } else {
                Log.v(TAG, "No su in: " + p);
            }
        }
        Log.d(TAG, "No su found in a well-known location, " + "will just use \"su\".");
        return "su";
    }
}
