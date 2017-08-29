package com.ewjmulder.iotadventure.service.model;

public interface Light<S extends LightState> {

	public String getName();
	
	public S getState();
	
	public void setState(S state);
	
}
