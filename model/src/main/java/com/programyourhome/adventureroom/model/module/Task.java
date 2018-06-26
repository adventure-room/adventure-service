package com.programyourhome.adventureroom.model.module;

@FunctionalInterface
public interface Task extends Runnable {

    public default boolean isDeamon() {
        return false;
    }

    public default boolean failOnException() {
        return true;
    }

}
