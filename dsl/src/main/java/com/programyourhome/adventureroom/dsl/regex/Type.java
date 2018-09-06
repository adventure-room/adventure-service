package com.programyourhome.adventureroom.dsl.regex;

public enum Type {

    ID("[a-z0-9]+"),
    ID_LIST(ID.regex + "(," + ID.regex + ")*"),
    INTEGER("[0-9]+"),
    DOUBLE("[0-9]+(\\.[0-9]+)?"),
    NAME("[a-z]+"),
    TEXT("[^\"]+"),
    DURATION("[0-9]+ [a-z]+");

    private String regex;

    private Type(String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return this.regex;
    }

}
