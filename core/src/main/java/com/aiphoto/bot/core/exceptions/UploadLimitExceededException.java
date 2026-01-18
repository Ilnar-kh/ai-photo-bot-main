package com.aiphoto.bot.core.exceptions;

public class UploadLimitExceededException extends RuntimeException {

    private final int limit;
    private final int current;

    public UploadLimitExceededException(int limit, int current) {
        super("Upload limit " + limit + " reached. Current: " + current);
        this.limit = limit;
        this.current = current;
    }

    public int getLimit() {
        return limit;
    }

    public int getCurrent() {
        return current;
    }
}