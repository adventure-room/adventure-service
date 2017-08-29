package com.ewjmulder.iotadventure.service;

import com.ewjmulder.iotadventure.service.model.Language;
import com.ewjmulder.iotadventure.service.model.Scene;

public interface Adventure {

	public void say(Language language, String text);
	
	public void setScene(Scene scene);
	
}
