package com.programyourhome.adventureroom.model.toolbox;

import java.io.InputStream;

public class DataStream {

    public static final int LENGTH_UNKNOWN = -1;

    private final InputStream inputStream;
    private final String contentType;
    private final long length;

    public DataStream(InputStream inputStream, String contentType) {
        this(inputStream, contentType, LENGTH_UNKNOWN);
    }

    public DataStream(InputStream inputStream, String contentType, long length) {
        this.inputStream = inputStream;
        this.contentType = contentType;
        this.length = length;
        if (length < 0 && length != LENGTH_UNKNOWN) {
            throw new IllegalArgumentException("Invalid length: " + length);
        }
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public String getContentType() {
        return this.contentType;
    }

    public boolean isLengthKnown() {
        return this.length != LENGTH_UNKNOWN;
    }

    public long getLength() {
        return this.length;
    }

}
