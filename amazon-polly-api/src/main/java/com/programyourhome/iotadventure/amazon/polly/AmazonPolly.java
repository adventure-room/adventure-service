package com.programyourhome.iotadventure.amazon.polly;

public interface AmazonPolly {

	default void say(String voiceId, String text) {
		sayText(voiceId, text);
	}
	
	public void sayText(String voiceId, String text);
	
	public void saySsml(String voiceId, String ssml);

}
