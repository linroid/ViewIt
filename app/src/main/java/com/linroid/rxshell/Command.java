package com.linroid.rxshell;

/**
 * @author linroid <linroid@gmail.com>
 * @since 25/01/2017
 */
class Command {
    int id;
    String cmd;
    int exitCode;
    CommandCallback callback;

    public Command(int id, String cmd, CommandCallback callback) {
        this.id = id;
        this.cmd = cmd;
        this.callback = callback;
    }
}
