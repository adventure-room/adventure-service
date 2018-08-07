package com.programyourhome.adventureroom.model.toolbox;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public abstract class CacheResource {

    private static MessageDigest DIGEST;
    static {
        try {
            DIGEST = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Invalid algorithm", e);
        }
    }

    public String name;
    public String extension;
    public String mimeType;

    public abstract String getId();

    public String getSanitizedId() {
        return this.sanitize(this.getId());
    }

    public String getSanitizedName() {
        return this.sanitize(this.name);
    }

    protected String sanitize(String input) {
        String sanitizedInput = input;
        sanitizedInput = sanitizedInput.replaceAll("[^a-zA-Z0-9-_ ]", "");
        sanitizedInput = sanitizedInput.replaceAll("[ ]+", "_");
        return sanitizedInput;
    }

    protected String checksum(byte[] bytes) {
        byte[] checksum = DIGEST.digest(bytes);
        return Base64.getEncoder().encodeToString(checksum);
    }

    protected String checksum(String input) {
        return this.checksum(input.getBytes(StandardCharsets.UTF_8));
    }

    public String getFilePath() {
        return this.getSanitizedId() + "--" + this.getSanitizedName() + "." + this.extension;
    }

}
