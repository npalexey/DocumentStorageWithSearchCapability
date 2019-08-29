package com.nikitiuk.documentstoragewithsearchcapability.exceptions;

public class NoValidDataFromJsonException extends Exception {

    public NoValidDataFromJsonException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoValidDataFromJsonException(String message) {
        super(message);
    }

    public NoValidDataFromJsonException(Throwable cause) {
        super(cause);
    }
}
