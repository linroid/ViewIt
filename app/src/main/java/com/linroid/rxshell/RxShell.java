package com.linroid.rxshell;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.HandlerThread;

import com.linroid.rxshell.exception.ShellExecuteErrorException;
import com.linroid.rxshell.exception.ShellTerminateException;
import com.linroid.viewit.App;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author linroid <linroid@gmail.com>
 * @since 17/01/2017
 */
public class RxShell {
    public static final String SP_BINARY_VERSIONS_FILE = "binary_versions";
    private final HandlerThread workerThread = new HandlerThread("RxShell-Worker");

    private final Shell shell;

    private static RxShell sInstance;

    public static RxShell instance() {
        if (sInstance == null) {
            sInstance = new RxShell(true);
        }
        return sInstance;
    }

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

    public Observable<Boolean> destroy() {
        return create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    subscriber.onNext(shell.destroy());
                    subscriber.onCompleted();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<String> exec(final String binary) {
        return exec(binary, "");
    }

    public Observable<String> exec(final String binary, final String arguments) {
        return create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                shell.exec(binary, arguments, new Shell.Callback() {
                    @Override
                    public void onOutput(String line) {
                        subscriber.onNext(line);
                    }

                    @Override
                    public void onTerminate() {
                        subscriber.onError(new ShellTerminateException());
                    }

                    @Override
                    public void onFinished() {
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onError(String output) {
                        subscriber.onError(new ShellExecuteErrorException(output));
                    }
                });
            }
        });
    }

    public <T> Observable<T> create(Observable.OnSubscribe<T> source) {
        return Observable.create(source).subscribeOn(AndroidSchedulers.from(workerThread.getLooper()));
    }

    public Observable<Boolean> installBinary(@NotNull final Context context, @NotNull String assetsPath, @NotNull final String name, float version) {
        return create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                File binaryDir = new File(context.getFilesDir(), "exec");
                if (!binaryDir.exists()) {
                    binaryDir.mkdirs();
                }
                File binaryFile = new File(binaryDir, name);
                SharedPreferences sp = context.getSharedPreferences(SP_BINARY_VERSIONS_FILE, Context.MODE_PRIVATE);
                float existsVersion = sp.getFloat(name, -1);
                if (!binaryFile.exists()) {

                }
            }
        });

    }
}
