package com.programyourhome.adventureroom.model.either;

import java.util.Optional;

import com.programyourhome.adventureroom.model.util.StreamUtil;

import one.util.streamex.StreamEx;

public class EitherOrNone {

    private final Optional<?>[] optionals;

    public EitherOrNone(Optional<?>... optionals) {
        this.optionals = optionals;
        this.assertZeroOrOne();
    }

    public Optional<?>[] getOptionals() {
        return this.optionals;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getItem(int oneBasedIndex) {
        return (Optional<T>) this.optionals[oneBasedIndex - 1];
    }

    public Optional<Yes> isEmpty() {
        if (this.getOptionalSize() == 0) {
            return Optional.of(Yes.Y);
        } else {
            return Optional.empty();
        }
    }

    public long getOptionalSize() {
        return StreamEx.of(this.optionals)
                .flatMap(StreamUtil::optionalToStream)
                .count();
    }

    private void assertZeroOrOne() {
        if (this.getOptionalSize() > 1) {
            throw new IllegalArgumentException("Either or none should have zero or one value in it's optionals, not " + this.getOptionalSize());
        }
    }

}
