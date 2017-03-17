package com.linroid.rxshell;

import android.util.Log;

import com.linroid.rxshell.exception.ShellExecuteErrorException;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.charset.Charset;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * @author linroid <linroid@gmail.com>
 * @since 17/01/2017
 */
class Shell {
    private static final String COMMAND_SU = "su";
    private static final String COMMAND_SH = "sh";
    private static final String COMMAND_EXIT = "exit\n";
    private static final String COMMAND_LINE_END = "\n";
    private static final String COMMAND_END = "echo 'end'\n";

    private static final String RESULT_COMMAND_FINISHED = "linorid";

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final Charset ASCII = Charset.forName("ASCII");
    private static final String TAG = "Shell";
    private boolean requireRoot;
    private BufferedSink writer;
    private BufferedSource reader;
    private BufferedSource errorReader;
    private Process connection;
    private AtomicInteger ID = new AtomicInteger(0);
    private Thread readerThread;

    private ArrayBlockingQueue<Command> queue = new ArrayBlockingQueue<Command>(100);

    Shell(boolean requireRoot) {
        this.requireRoot = requireRoot;
        connect();
    }

    Shell(String customShell) {

    }

    private boolean connect() {
        try {
            ProcessBuilder builder = new ProcessBuilder(requireRoot ? Utils.getSuPath() : COMMAND_SH);
            builder.redirectErrorStream(true);
            builder.command();

            connection = builder.start();
            writer = Okio.buffer(Okio.sink(connection.getOutputStream()));
            reader = Okio.buffer(Okio.source(connection.getInputStream()));
            errorReader = Okio.buffer(Okio.source(connection.getErrorStream()));
            startReaderThread();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            return false;
        }
        return true;
    }

    private void startReaderThread() {
        readerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String line;
                int exitCode = 0;
                int exitCmdId = -1;
                Command current = null;
                try {
                    current = queue.take();
                    while (reader != null && (line = reader.readUtf8Line()) != null) {
                        if (line.startsWith(RESULT_COMMAND_FINISHED)) {
                            String[] res = line.split(" ");
                            exitCmdId = Integer.valueOf(res[1]);
                            exitCode = Integer.valueOf(res[2]);
                            if (exitCmdId != current.id) {
                                current.callback.onError(new ShellExecuteErrorException("current command:" + current.id + " not equals exit id:" + exitCmdId));
                                destroy();
                                break;
                            }
                            current.exitCode = exitCode;
                            current.callback.onFinished(current);
                            current = queue.take();
                            continue;
                        }
                        current.callback.onOutput(line);
                    }
                } catch (InterruptedException | InterruptedIOException error) {
                    if (current != null) {
                        current.callback.onError(error);
                    }
                    while (!queue.isEmpty()) {
                        Command command = null;
                        try {
                            command = queue.take();
                            command.callback.onError(error);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                    destroy();
                } catch (IOException e) {
                    e.printStackTrace();
                    if (current != null) {
                        current.callback.onError(e);
                    }
                    destroy();
                }
            }
        }, "shell-reader");
        readerThread.start();
    }

    void exec(String binary, String args, CommandCallback callback) {
        exec(binary + " " + args, callback);
    }

    private void exec(String cmd, CommandCallback callback) {
        if (!checkConnection()) {
            callback.onError(new ShellExecuteErrorException("device not root"));
            return;
        }
        int commandId = ID.incrementAndGet();
        Command command = new Command(commandId, cmd, callback);
        Log.v(TAG, "execute command[" + commandId + "]: " + command.cmd + "");
        try {
            queue.put(command);
            writer.writeString(command.cmd, UTF_8);
            writer.write(COMMAND_LINE_END.getBytes());
            writer.write(COMMAND_END.getBytes());
            writer.write(("  echo " + RESULT_COMMAND_FINISHED + " " + commandId + " $?\n").getBytes());
            writer.flush();
        } catch (InterruptedException | IOException error) {
            error.printStackTrace();
            destroy();
            command.callback.onError(error);
        }
    }

    private boolean checkConnection() {
        if (connection == null) {
            return connect();
        }
        return true;
    }

    boolean destroy() {
        try {
            writer.writeString(COMMAND_EXIT, UTF_8);
            connection.waitFor();
            writer.close();
            reader.close();
            errorReader.close();
            connection.destroy();
        } catch (InterruptedException | IOException error) {
            error.printStackTrace();
        } finally {
            writer = null;
            reader = null;
            errorReader = null;
            connection = null;
        }

        try {
            if (readerThread != null) {
                readerThread.interrupt();
            }
        } finally {
            readerThread = null;
        }
        return true;
    }
}
