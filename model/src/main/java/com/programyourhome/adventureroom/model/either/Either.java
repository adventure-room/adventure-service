package com.programyourhome.adventureroom.model.either;

import java.util.Optional;

import com.programyourhome.adventureroom.model.util.StreamUtil;

import one.util.streamex.StreamEx;

public class Either {

    private final Optional<?>[] optionals;

    public Either(Optional<?>... optionals) {
        this.optionals = optionals;
        this.assertOneAndJustOne();
    }

    public Optional<?>[] getOptionals() {
        return this.optionals;
    }

    public int getNumberOfOptionals() {
        return this.optionals.length;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getItem(int oneBasedIndex) {
        return (Optional<T>) this.optionals[oneBasedIndex - 1];
    }

    public long getNumberOfNonEmptyOptionals() {
        return StreamEx.of(this.optionals)
                .flatMap(StreamUtil::optionalToStream)
                .count();
    }

    private void assertOneAndJustOne() {
        if (this.getNumberOfNonEmptyOptionals() > 1) {
            throw new IllegalArgumentException("Either should have one and just one value in it's optionals, not " + this.getNumberOfNonEmptyOptionals());
        }
    }

}
