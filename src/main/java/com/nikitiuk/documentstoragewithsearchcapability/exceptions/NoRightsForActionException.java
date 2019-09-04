package com.nikitiuk.documentstoragewithsearchcapability.exceptions;

public class NoRightsForActionException extends Exception {

    public NoRightsForActionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoRightsForActionException(String message) {
        super(message);
    }

    public NoRightsForActionException(Throwable cause) {
        super(cause);
    }
}