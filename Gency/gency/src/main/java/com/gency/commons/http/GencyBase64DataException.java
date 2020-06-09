package com.gency.commons.http;

import java.io.IOException;

public class GencyBase64DataException extends IOException {
    public GencyBase64DataException(String detailMessage) {
        super(detailMessage);
    }
}