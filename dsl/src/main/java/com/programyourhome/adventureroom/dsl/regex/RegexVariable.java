package com.programyourhome.adventureroom.dsl.regex;

public class RegexVariable {

    public String name;
    public String prefix;
    public String regex;
    public String postfix;

    public RegexVariable(String name, Type type) {
        this(name, type.getPrefix(), type.getRegex(), type.getPostfix());
    }

    public RegexVariable(String name, String regex) {
        this(name, "", regex, "");
    }

    public RegexVariable(String name, String prefix, String regex, String postfix) {
        this.name = name;
        this.prefix = prefix;
        this.regex = regex;
        this.postfix = postfix;
    }

    @Override
    public String toString() {
        return this.prefix + "(?<" + this.name + ">" + this.regex + ")" + this.postfix;
    }

    public RegexVariable withName(String name) {
        return new RegexVariable(name, this.prefix, this.regex, this.postfix);
    }

}
