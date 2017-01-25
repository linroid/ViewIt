package com.linroid.rxshell;

/**
 * @author linroid <linroid@gmail.com>
 * @since 25/01/2017
 */
interface CommandCallback {
    void onOutput(String line);

    void onTerminate();

    void onFinished(Command command);

    void onError(Exception error);
}
