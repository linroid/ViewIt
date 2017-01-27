package com.linroid.rxshell;

import android.support.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author linroid <linroid@gmail.com>
 * @since 23/01/2017
 */
class IOUtils {
    static void closeQuietly(@Nullable Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
