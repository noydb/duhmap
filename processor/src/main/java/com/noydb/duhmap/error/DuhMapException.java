package com.noydb.duhmap.error;

public class DuhMapException extends RuntimeException {

    public DuhMapException(String message) {
        super(message);
    }

    public DuhMapException(Throwable cause) {
        super(cause);
    }

}
