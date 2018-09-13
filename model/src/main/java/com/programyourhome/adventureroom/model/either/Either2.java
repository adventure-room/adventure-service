package com.programyourhome.adventureroom.model.either;

import java.util.Optional;

public class Either2<First, Second> extends EitherSome {

    public Either2(Optional<First> first, Optional<Second> second) {
        super(first, second);
    }

    public Optional<First> getFirst() {
        return this.getItem(1);
    }

    public Optional<Second> getSecond() {
        return this.getItem(2);
    }

}
