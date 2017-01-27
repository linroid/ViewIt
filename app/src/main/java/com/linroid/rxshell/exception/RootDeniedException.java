package com.linroid.rxshell.exception;

public class RootDeniedException extends Exception {
    public RootDeniedException() {
        super("root permission denied");
    }
}