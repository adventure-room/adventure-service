package com.programyourhome.adventureroom.server.controllers;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.programyourhome.adventureroom.model.toolbox.CacheService;

@RestController
@RequestMapping(CacheController.PATH_PREFIX)
public class CacheController {

    public static final String PATH_PREFIX = "cache";

    @Inject
    private CacheService cacheService;

    // @RequestMapping(value = "{id}")
    // public ResponseEntity<byte[]> getResource(@PathVariable("id") final String id) throws IOException {
    // // if (this.cacheService)
    // byte[] audioBytes = IOUtils.toByteArray(new FileInputStream(new File(this.audioBasepath + "/" + id + ".wav")));
    // MultiValueMap<String, String> headers = new MultiValueMap<>();
    // // headers.put("Content-Type", value)
    // ResponseEntity responseEntity = new ResponseEntity<byte[]>(audioBytes, HttpStatus.OK);
    // return responseEntity;
    // }

}
