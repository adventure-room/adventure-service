package com.programyourhome.iotadventure.common.config.validation;

import java.util.Optional;
import java.util.function.Function;

public interface ValidationRule<ConfigType> extends Function<ConfigType, Optional<String>> {

}
