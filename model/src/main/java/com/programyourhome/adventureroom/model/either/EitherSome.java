package com.programyourhome.adventureroom.model.either;

import java.util.Optional;

public class EitherSome extends EitherOrNone {

    public EitherSome(Optional<?>... optionals) {
        super(optionals);
        this.assertOneAndJustOne();
    }

    private void assertOneAndJustOne() {
        if (this.getOptionalSize() > 1) {
            throw new IllegalArgumentException("Either should have one and just one value in it's optionals, not " + this.getOptionalSize());
        }
    }

}
