package com.programyourhome.adventureroom.model.util;

import java.util.Optional;

import one.util.streamex.StreamEx;

/**
 * Util methods for Stream(Ex).
 */
public class StreamUtil {

    private StreamUtil() {
    }

    /**
     * Convert an Optional into a Stream.
     * If present, a stream with that one element. If not, an empty stream.
     */
    public static <T> StreamEx<T> optionalToStream(Optional<T> optional) {
        return optional.isPresent() ? StreamEx.of(optional.get()) : StreamEx.empty();
    }

    @SafeVarargs
    public static final <T> Optional<T> getOneAsOptional(Optional<T>... optionals) {
        return Optional.of(getOne(optionals));
    }

    @SafeVarargs
    public static final <T> T getOne(Optional<T>... optionals) {
        return maybeOne(optionals).get();
    }

    @SafeVarargs
    public static final <T> Optional<T> maybeOne(Optional<T>... optionals) {
        return StreamEx.of(optionals)
                .flatMap(StreamUtil::optionalToStream)
                .findFirst();
    }

}
