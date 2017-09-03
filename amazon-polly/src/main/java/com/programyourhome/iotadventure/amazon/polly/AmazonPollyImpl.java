package com.programyourhome.iotadventure.amazon.polly;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.TextType;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

@Component
public class AmazonPollyImpl implements AmazonPolly {

	// TODO: prevent naming clash?
	private com.amazonaws.services.polly.AmazonPolly pollyClient;
	
	@PostConstruct
	public void init() {
		// create an Amazon Polly client in a specific region
		pollyClient = AmazonPollyClient.builder()
				.withCredentials(new DefaultAWSCredentialsProviderChain())
				.withClientConfiguration(new ClientConfiguration())
				.withRegion(Regions.EU_WEST_1)
				.build();
	}
	
	@Override
	public void sayText(String voiceId, String text) {
		synthesize(voiceId, TextType.Text, text);
	}
	
	@Override
	public void saySsml(String voiceId, String ssml) {
		synthesize(voiceId, TextType.Ssml, ssml);		
	}
	
	public void synthesize(String voiceId, TextType textType, String input) {
		SynthesizeSpeechRequest synthReq = 
		new SynthesizeSpeechRequest()
				.withText(input)
				.withTextType(textType)
				.withVoiceId(voiceId)
				.withOutputFormat(OutputFormat.Mp3);
		SynthesizeSpeechResult synthRes = pollyClient.synthesizeSpeech(synthReq);

		//TODO: split synthesis and playback
			
		try {
			AdvancedPlayer player = new AdvancedPlayer(synthRes.getAudioStream(),
					javazoom.jl.player.FactoryRegistry.systemRegistry().createAudioDevice());
	
			player.setPlayBackListener(new PlaybackListener() {
				@Override
				public void playbackStarted(PlaybackEvent evt) {
					System.out.println("Playback started");
					System.out.println(input);
				}
				
				@Override
				public void playbackFinished(PlaybackEvent evt) {
					System.out.println("Playback finished");
				}
			});
			
			// play it!
			player.play();
		} catch (JavaLayerException e) {
			throw new IllegalStateException("Exception occured during playback", e);
		}
	}

}
