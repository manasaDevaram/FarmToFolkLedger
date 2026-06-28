package com.farmtofolk.farmtofolk_ledger.common.error;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
