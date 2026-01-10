package com.bytemarket.bytemarket_api.exceptions;

public class OutOfStockException  extends RuntimeException {
    public OutOfStockException (String message) {
        super(message);
    }
}
