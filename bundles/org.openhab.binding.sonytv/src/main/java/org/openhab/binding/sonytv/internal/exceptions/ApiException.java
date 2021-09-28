package org.openhab.binding.sonytv.internal.exceptions;

public class ApiException extends Exception {
    protected int code;

    public ApiException(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
