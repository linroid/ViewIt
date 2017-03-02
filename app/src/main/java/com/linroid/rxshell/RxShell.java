package com.linroid.rxshell;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.HandlerThread;
import android.support.annotation.CheckResult;
import android.util.Log;

import com.linroid.rxshell.exception.ShellTerminateException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author linroid <linroid@gmail.com>
 * @since 17/01/2017
 */
public class RxShell {
    private static final String TAG = "RxShell";
    private static final String SP_BINARY_VERSIONS_FILE = "binary_versions";
    private final HandlerThread workerThread = new HandlerThread("RxShell-Worker");

    private final Shell shell;

    public RxShell(String customShell) {
        this.shell = new Shell(customShell);
        init();
    }

    public RxShell(boolean requireRoot) {
        this.shell = new Shell(requireRoot);
        init();
    }

    private void init() {
        workerThread.start();
    }

    @CheckResult
    public Observable<Boolean> destroy() {
        return create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    subscriber.onNext(shell.destroy());
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });
    }

    @CheckResult
    public Observable<String> execWithResult(final String binary) {
        return execWithResult(binary, "");
    }

    @CheckResult
    public Observable<String> execWithResult(final String binary, final String arguments) {
        return create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                shell.exec(binary, arguments, new CommandCallback() {
                    @Override
                    public void onOutput(String line) {
                        subscriber.onNext(line);
                    }

                    @Override
                    public void onTerminate() {
                        subscriber.onError(new ShellTerminateException());
                    }

                    @Override
                    public void onFinished(Command command) {
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onError(Exception error) {
                        subscriber.onError(error);
                    }
                });
            }
        });
    }

    @CheckResult
    public Observable<Boolean> execWithoutResult(final String binary) {
        return execWithoutResult(binary, "");
    }

    @CheckResult
    public Observable<Boolean> execWithoutResult(final String cmd, final String... args) {
        final StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(arg);
        }
        return create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                shell.exec(cmd, sb.toString(), new CommandCallback() {
                    @Override
                    public void onOutput(String line) {
                    }

                    @Override
                    public void onTerminate() {
                        subscriber.onError(new ShellTerminateException());
                    }

                    @Override
                    public void onFinished(Command command) {
                        subscriber.onNext(true);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onError(Exception error) {
                        subscriber.onError(error);
                    }
                });
            }
        });
    }

    @CheckResult
    public <T> Observable<T> create(Observable.OnSubscribe<T> source) {
        return Observable.create(source)
                .subscribeOn(AndroidSchedulers.from(workerThread.getLooper()));
    }

    @CheckResult
    public Observable<Boolean> installBinary(@NotNull final Context context, @NotNull final InputStream stream, @NotNull final String binaryName, final float version) {
        Log.d(TAG, "install binary: " + binaryName);
        return create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                File binaryDir = new File(context.getFilesDir(), "exec");
                if (!binaryDir.exists()) {
                    binaryDir.mkdirs();
                }
                File binaryFile = new File(binaryDir, binaryName);
                SharedPreferences sp = context.getSharedPreferences(SP_BINARY_VERSIONS_FILE, Context.MODE_PRIVATE);
                float existsVersion = sp.getFloat(binaryName, -1);
                if (!binaryFile.exists() || existsVersion < version || !binaryFile.canExecute()) {
                    BufferedSource source = null;
                    BufferedSink sink = null;
                    if (binaryFile.exists()) {
                        binaryFile.delete();
                    }
                    try {
                        source = Okio.buffer(Okio.source(stream));
                        sink = Okio.buffer(Okio.sink(binaryFile));
                        source.readAll(sink);
//                        sp.edit().putFloat(SP_BINARY_VERSIONS_FILE, version).commit();
                        subscriber.onNext(binaryFile.setExecutable(true));
                        subscriber.onCompleted();
                    } catch (IOException e) {
                        Log.e(TAG, "error when install binary " + binaryName, e);
                        subscriber.onError(e);
                    } finally {
                        IOUtils.closeQuietly(sink);
                        IOUtils.closeQuietly(source);
                    }
                } else {
                    subscriber.onNext(true);
                    subscriber.onCompleted();
                }
            }
        });
    }

    public Observable<String> execBinary(@NotNull Context context, @NotNull String binaryName, String arguments) {
        File binaryDir = new File(context.getFilesDir(), "exec");
        File binaryFile = new File(binaryDir, binaryName);
        if (!binaryFile.exists() || !binaryFile.canExecute()) {
            return Observable.just(null);
        }
        return execWithResult(binaryFile.getAbsolutePath(), arguments);
    }

    @NotNull
    public Observable<Boolean> copyFile(@NotNull final String path, final String targetPath) {
        return execWithoutResult("cat", path, ">", targetPath);
    }

    @NotNull
    public Observable<Boolean> chown(@Nullable final String path, final int uid, final int group) {
        return execWithoutResult("chown", uid + ":" + group, path);

    }

    public Observable<Boolean> deleteFile(@Nullable String path) {
        return execWithoutResult("rm -rf", path);
    }

    public boolean binaryExists(@NotNull Context context, @NotNull String binaryName) {
        File binaryDir = new File(context.getFilesDir(), "exec");
        File binaryFile = new File(binaryDir, binaryName);
        return binaryFile.exists() && binaryFile.canExecute();
    }
}
