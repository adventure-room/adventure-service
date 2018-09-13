package com.programyourhome.adventureroom.model.either;

import java.util.Optional;

public class Either4<First, Second, Third, Fourth> extends EitherSome {

    public Either4(Optional<First> first, Optional<Second> second, Optional<Third> third, Optional<Fourth> fourth) {
        super(first, second, third, fourth);
    }

}
