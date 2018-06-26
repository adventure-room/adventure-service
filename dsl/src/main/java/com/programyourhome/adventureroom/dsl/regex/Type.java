package com.programyourhome.adventureroom.dsl.regex;

public enum Type {

    ID("[a-z0-9]+"),
    INTEGER("[0-9]+"),
    DOUBLE("[0-9]+(\\.[0-9]+)?"),
    NAME("[a-z]+"),
    TEXT("[^\"]+");

    private String regex;

    private Type(String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return this.regex;
    }

}
