package com.programyourhome.adventureroom.dsl.regex;

public enum Type {

    // NB: keep in mind that using previously defined types with '+' only takes the type's regex, without prefix and postfix.
    ID("[a-z0-9]+"),
    ID_LIST(ID + "(," + ID + ")*"),
    INTEGER("[0-9]+"),
    DOUBLE(INTEGER + "(\\." + INTEGER + ")?"),
    NAME("[a-z]+"),
    NAME_WITH_DASHES("[a-z\\-]+"),
    WORD("[^ ]+"),
    FILENAME("[A-Za-z\\.]+"),
    TEXT("\"", "[^\"]+", "\""),
    DURATION(INTEGER + " " + NAME),
    LOCATION(DOUBLE + "," + DOUBLE + "," + DOUBLE),
    LOCATION_PATH(LOCATION + "(;" + LOCATION + ")*");

    private String prefix;
    private String regex;
    private String postfix;

    private Type(String regex) {
        this("", regex, "");
    }

    private Type(String prefix, String regex, String postfix) {
        this.prefix = prefix;
        this.regex = regex;
        this.postfix = postfix;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getPostfix() {
        return this.postfix;
    }

    public String getRegex() {
        return this.regex;
    }

    /**
     * Returns just the regex, without prefix and postfix.
     */
    @Override
    public String toString() {
        return this.regex;
    }

}
