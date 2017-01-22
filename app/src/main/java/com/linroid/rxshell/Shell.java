package com.linroid.rxshell;

import android.text.TextUtils;
import android.util.Log;

import com.linroid.rxshell.exception.ShellExecuteErrorException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * @author linroid <linroid@gmail.com>
 * @since 17/01/2017
 */
public class Shell {
    public static final String COMMAND_SU = "su";
    public static final String COMMAND_SH = "sh";
    public static final String COMMAND_EXIT = "exit\n";
    public static final String COMMAND_LINE_END = "\n";
    public static final String COMMAND_END = "echo 'end'\n";

    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final String TAG = "Shell";
    private boolean requireRoot;
    private BufferedSink writer;
    private BufferedSource reader;
    private BufferedSource errorReader;
    private Process connection;

    public Shell(boolean requireRoot) {
        this.requireRoot = requireRoot;
        connect();
    }

    public Shell(String customShell) {

    }

    public boolean connect() {
        try {
            ProcessBuilder builder = new ProcessBuilder(requireRoot ? COMMAND_SU : COMMAND_SH);
            builder.redirectErrorStream(true);
            builder.command();

            connection = builder.start();
            writer = Okio.buffer(Okio.sink(connection.getOutputStream()));
            reader = Okio.buffer(Okio.source(connection.getInputStream()));
            errorReader = Okio.buffer(Okio.source(connection.getErrorStream()));
//            writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
//            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));


        } catch (IOException e) {
            Log.w(TAG, e.getMessage(), e);
            return false;
        }
        return true;
    }

    public void disconnect() {
        try {
            writer.close();
            reader.close();
            errorReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.destroy();
    }

    public void exec(String binary, String args, Callback callback) {
        exec(binary + " " + args, callback);
    }

    public void exec(String command, Callback callback) {
        if (connection == null) {
            callback.onError("device not root");
            return;
        }
        try {
            writer.writeString(command, UTF_8);
            writer.write(COMMAND_LINE_END.getBytes());
            writer.write(COMMAND_END.getBytes());
            writer.flush();
            connection.waitFor();
            Log.d(TAG, "exec command = [" + command + "]");
            String line;
            while ((line = reader.readUtf8Line()) != null) {
                callback.onOutput(line);
            }
            while ((line = errorReader.readUtf8Line()) != null) {
                callback.onError(line);
            }
            callback.onFinished();
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    Log.w(TAG, "start reader");
//                    try {
//                        String line;
//                        while ((line = reader.readLine()) != null) {
//                            Log.i(TAG, "read line : " + line);
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    Log.w(TAG, "end reader");
//                }
//            }).start();
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    Log.w(TAG, "start error reader");
//                    try {
//                        String line;
//                        while ((line = errorReader.readLine()) != null) {
//                            Log.i(TAG, "read error line : " + line);
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    Log.w(TAG, "end error reader");
//                }
//            }).start();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            callback.onError(e.getMessage());
        }
    }

    public Boolean destroy() throws InterruptedException {
        connection.waitFor();
        connection.destroy();
        return true;
    }

    public interface Callback {
        void onOutput(String line);

        void onTerminate();

        void onFinished();

        void onError(String output);
    }

}
