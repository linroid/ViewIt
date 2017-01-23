package com.linroid.rxshell;

import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

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

    public static final String RESULT_COMMAND_FINISHED = "linorid";

    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final Charset ASCII = Charset.forName("ASCII");
    public static final String TAG = "Shell";
    private boolean requireRoot;
    private BufferedSink writer;
    private BufferedSource reader;
    private BufferedSource errorReader;
    private Process connection;
    private AtomicInteger ID = new AtomicInteger(0);

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
        int commandId = ID.incrementAndGet();
        Log.v(TAG, "exec command[" + commandId + "]: " + command + "");
        try {
            writer.writeString(command, UTF_8);
            writer.write(COMMAND_LINE_END.getBytes());
            writer.write(COMMAND_END.getBytes());
            writer.write(("  echo " + RESULT_COMMAND_FINISHED + " " + commandId + " $?\n").getBytes());
            writer.flush();
//            connection.waitFor();
            String line;
            int exitCode = 0;
            int exitCmdId = -1;
            while ((line = reader.readUtf8Line()) != null) {
                if (line.startsWith(RESULT_COMMAND_FINISHED)) {
                    String[] res = line.split(" ");
                    exitCmdId = Integer.valueOf(res[1]);
                    exitCode = Integer.valueOf(res[2]);
                    break;
                }
                callback.onOutput(line);
            }
            while ((line = errorReader.readUtf8Line()) != null) {
                callback.onError(line);
            }
            callback.onFinished();
            Log.v(TAG, "command[" + exitCmdId + "] finished with " + exitCode);
        } catch (IOException e) {
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
