package com.noydb.duhmap.error;

public class DuhMapProcessorException extends RuntimeException {


    public DuhMapProcessorException(String message) {
        super(message);
    }

    public DuhMapProcessorException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuhMapProcessorException(Throwable cause) {
        super(cause);
    }

    public DuhMapProcessorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
