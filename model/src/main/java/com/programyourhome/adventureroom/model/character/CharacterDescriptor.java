package com.programyourhome.adventureroom.model.character;

import java.util.Set;

import com.programyourhome.adventureroom.model.AbstractTypeDescribable;
import com.programyourhome.adventureroom.model.Language;

public class CharacterDescriptor<C extends Character> extends AbstractTypeDescribable<C> {

    public String textToSpeechService;

    public Set<Language> supportedLanguages;

}
