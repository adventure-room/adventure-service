package com.programyourhome.adventureroom.model.either;

import java.util.Optional;

public class Either3<First, Second, Third> extends EitherSome {

    public Either3(Optional<First> first, Optional<Second> second, Optional<Third> third) {
        super(first, second, third);
    }

    public Optional<First> getFirst() {
        return this.getItem(1);
    }

    public Optional<Second> getSecond() {
        return this.getItem(2);
    }

    public Optional<Third> getThird() {
        return this.getItem(3);
    }

}
