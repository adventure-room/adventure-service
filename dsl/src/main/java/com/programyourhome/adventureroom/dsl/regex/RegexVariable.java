package com.programyourhome.adventureroom.dsl.regex;

public class RegexVariable {

    public String name;
    public Type type;

    public RegexVariable(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        String regex = "(?<" + this.name + ">" + this.type.getRegex() + ")";
        if (this.type == Type.TEXT) {
            regex = "\"" + regex + "\"";
        }
        return regex;
    }

    public RegexVariable withName(String name) {
        return new RegexVariable(name, this.type);
    }

}
