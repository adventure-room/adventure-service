package com.programyourhome.iotadventure.model.event;

public class ButtonPressedEvent extends Event {

    private final String buttonId;

    public ButtonPressedEvent(String buttonId) {
        super("Button " + buttonId + " pressed");
        this.buttonId = buttonId;
    }

    public String getButtonId() {
        return this.buttonId;
    }

}
