package org.openhab.binding.sonytv.internal.exceptions;

public class UnexpectedResponseException extends Exception {
    protected int code;

    public UnexpectedResponseException(String message) {
        super(message);
    }
}
