package com.exelynt.booking.exception;

/** Thrown when a USER tries to access/modify a reservation that isn't theirs. */
public class AccessDeniedOwnershipException extends RuntimeException {
    public AccessDeniedOwnershipException(String message) {
        super(message);
    }
}
