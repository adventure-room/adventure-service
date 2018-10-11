package com.programyourhome.adventureroom.model.script;

public enum ScriptType {

    /**
     * A scene is a script that tells a part of the story with a list of actions. A scene will always have the same running time.
     * A scene will stop after the last (synchronous) action has been completed.
     * No 2 scenes can be ran at the same time and a scene and an interaction cannot be active at the same time.
     */
    SCENE,
    /**
     * An interaction is a moment in the story where input from an event (triggered by the user/player or otherwise) is needed to continue.
     * Therefore, an interaction can only be stopped by an event.
     * Typically some kind of audio-visual loop is active during an interaction and there may be time triggered actions/effects happening.
     * A scene and an interaction cannot be active at the same time, but 2 interactions could be active together.
     */
    INTERACTION,
    /**
     * An effect is a (typically small) sub-script that runs as part of a scene or interaction. It consist of a group of actions
     * that 'play out' some effect as part of the story. Effects could be seen as 'subroutines' or functions and often take parameters.
     * An effect will stop after the last (synchronous) action has been completed.
     * A typical example of an effect is an explosion with sound and light actions representing the explosion together at a certain place and time.
     */
    EFFECT;

}
