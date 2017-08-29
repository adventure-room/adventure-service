package com.ewjmulder.iotadventure.service.model.hue;

import com.ewjmulder.iotadventure.service.model.Light;

public class HueLight implements Light<HueLightState> {

	private String name;
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public HueLightState getState() {
		// TODO: Talk to hue bridge / API
		return null;
	}
	
	public void setState(HueLightState state) {
		// TODO: Talk to hue bridge / API
	}

}
