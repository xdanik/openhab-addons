package org.openhab.binding.sonytv.internal.exceptions;

public class ConnectionException extends Exception {
    public ConnectionException(String message, Throwable e) {
        super(message, e);
    }
}
