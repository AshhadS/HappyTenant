package com.example.tenantapp.util;

public abstract class Result<T> {

    // ---- Factory methods ----
    public static <T> Success<T> ok(T data) {
        return new Success<>(data);
    }

    public static <T> Error<T> fail(String message) {
        return new Error<>(message);
    }

    // ---- Convenience methods ----
    public boolean isOk() { return this instanceof Success; }
    public boolean isError() { return this instanceof Error; }

    public T get() {
        if (this instanceof Success) {
            return ((Success<T>) this).data;
        }
        return null;
    }

    public String getError() {
        if (this instanceof Error) {
            return ((Error<T>) this).message;
        }
        return null;
    }

    // ---- Subclasses ----
    public static final class Success<T> extends Result<T> {
        private final T data;
        public Success(T data) { this.data = data; }
        public T getData() { return data; }
    }

    public static final class Error<T> extends Result<T> {
        private final String message;
        public Error(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}
