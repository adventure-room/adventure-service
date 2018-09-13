package com.programyourhome.adventureroom.model.either;

import java.util.Optional;

public class Either {

    public static <First, Second> Either2<First, Second> firstOf2(First first) {
        return new Either2<>(Optional.of(first), Optional.empty());
    }

    public static <First, Second> Either2<First, Second> secondOf2(Second second) {
        return new Either2<>(Optional.empty(), Optional.of(second));
    }

    public static <First, Second, Third> Either3<First, Second, Third> firstOf3(First first) {
        return new Either3<>(Optional.of(first), Optional.empty(), Optional.empty());
    }

    public static <First, Second, Third> Either3<First, Second, Third> secondOf3(Second second) {
        return new Either3<>(Optional.empty(), Optional.of(second), Optional.empty());
    }

    public static <First, Second, Third> Either3<First, Second, Third> thirdOf3(Third third) {
        return new Either3<>(Optional.empty(), Optional.empty(), Optional.of(third));
    }

    public static <First, Second, Third, Fourth> Either4<First, Second, Third, Fourth> firstOf4(First first) {
        return new Either4<>(Optional.of(first), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static <First, Second, Third, Fourth> Either4<First, Second, Third, Fourth> secondOf4(Second second) {
        return new Either4<>(Optional.empty(), Optional.of(second), Optional.empty(), Optional.empty());
    }

    public static <First, Second, Third, Fourth> Either4<First, Second, Third, Fourth> thirdOf4(Third third) {
        return new Either4<>(Optional.empty(), Optional.empty(), Optional.of(third), Optional.empty());
    }

    public static <First, Second, Third, Fourth> Either4<First, Second, Third, Fourth> fourthOf4(Fourth fourth) {
        return new Either4<>(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(fourth));
    }

    public static <First, Second, Third, Fourth, Fifth> Either5<First, Second, Third, Fourth, Fifth> firstOf5(First first) {
        return new Either5<>(Optional.of(first), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static <First, Second, Third, Fourth, Fifth> Either5<First, Second, Third, Fourth, Fifth> secondOf5(Second second) {
        return new Either5<>(Optional.empty(), Optional.of(second), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static <First, Second, Third, Fourth, Fifth> Either5<First, Second, Third, Fourth, Fifth> thirdOf5(Third third) {
        return new Either5<>(Optional.empty(), Optional.empty(), Optional.of(third), Optional.empty(), Optional.empty());
    }

    public static <First, Second, Third, Fourth, Fifth> Either5<First, Second, Third, Fourth, Fifth> fourthOf5(Fourth fourth) {
        return new Either5<>(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(fourth), Optional.empty());
    }

    public static <First, Second, Third, Fourth, Fifth> Either5<First, Second, Third, Fourth, Fifth> fifthOf5(Fifth fifth) {
        return new Either5<>(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(fifth));
    }

}
