package com.programyourhome.adventureroom.model.either;

import java.util.Optional;

public class Either5<First, Second, Third, Fourth, Fifth> extends EitherSome {

    public Either5(Optional<First> first, Optional<Second> second, Optional<Third> third, Optional<Fourth> fourth, Optional<Fifth> fifth) {
        super(first, second, third, fourth, fifth);
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

    public Optional<Fourth> getFourth() {
        return this.getItem(4);
    }

    public Optional<Fourth> getFifth() {
        return this.getItem(5);
    }

}
