package com.programyourhome.adventureroom.server.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("audio")
public class AudioController {

    @Value("${audio.basepath}")
    private File audioBasepath;

    @PostConstruct
    public void init() {
        if (!this.audioBasepath.exists()) {
            throw new IllegalStateException("Audio base path [" + this.audioBasepath.getAbsolutePath() + "] does not exist");
        }
    }

    @RequestMapping(value = "{id}", produces = "audio/wav")
    public ResponseEntity<byte[]> getAudioFile(@PathVariable("id") final String id) throws IOException {
        byte[] audioBytes = IOUtils.toByteArray(new FileInputStream(new File(this.audioBasepath + "/" + id + ".wav")));
        return new ResponseEntity<byte[]>(audioBytes, HttpStatus.OK);
    }

}
