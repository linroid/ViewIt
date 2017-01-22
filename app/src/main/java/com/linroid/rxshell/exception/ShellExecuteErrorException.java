package com.linroid.rxshell.exception;

/**
 * @author linroid <linroid@gmail.com>
 * @since 17/01/2017
 */
public class ShellExecuteErrorException extends Exception {
    public ShellExecuteErrorException(String message) {
        super(message);
    }

    public ShellExecuteErrorException() {
    }

    public ShellExecuteErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ShellExecuteErrorException(Throwable cause) {
        super(cause);
    }
}
