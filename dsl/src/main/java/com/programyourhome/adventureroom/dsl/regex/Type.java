package com.programyourhome.adventureroom.dsl.regex;

public enum Type {

    ID("[a-z0-9]+"),
    ID_LIST(ID + "(," + ID + ")*"),
    INTEGER("[0-9]+"),
    DOUBLE(INTEGER + "(\\." + INTEGER + ")?"),
    NAME("[a-z]+"),
    NAME_WITH_DASHES("[a-z\\-]+"),
    WORD("[^ ]+"),
    FILENAME("[A-Za-z\\.]+"),
    TEXT("[^\"]+"),
    DURATION(INTEGER + " " + NAME),
    LOCATION(DOUBLE + "," + DOUBLE + "," + DOUBLE);

    private String regex;

    private Type(String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return this.regex;
    }

    @Override
    public String toString() {
        return this.regex;
    }

}
