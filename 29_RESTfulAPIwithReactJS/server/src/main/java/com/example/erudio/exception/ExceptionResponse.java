package com.example.erudio.exception;

import java.util.Date;

public record ExceptionResponse(Date timestamp, String message, String detalis) {
}
