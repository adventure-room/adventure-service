package com.programyourhome.adventureroom.server.controllers;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.programyourhome.adventureroom.model.toolbox.DataStream;
import com.programyourhome.adventureroom.model.toolbox.DataStreamToUrl;

@RestController
@RequestMapping(DataStreamToUrlController.PATH_PREFIX)
public class DataStreamToUrlController implements DataStreamToUrl {

    public static final String PATH_PREFIX = "streams";

    private final Map<String, DataStream> dataStreams;

    public DataStreamToUrlController() {
        this.dataStreams = new HashMap<>();
    }

    @GetMapping("{id}")
    public ResponseEntity<InputStreamResource> getStream(@PathVariable("id") String id) {
        // TODO: throw exception if not found.
        DataStream dataStream = this.dataStreams.get(id);
        InputStreamResource inputStreamResource = new InputStreamResource(dataStream.getInputStream());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", dataStream.getContentType());
        headers.setContentLength(dataStream.getLength());
        return new ResponseEntity<InputStreamResource>(inputStreamResource, headers, HttpStatus.OK);
    }

    @Override
    public URL exposeDataStream(DataStream dataStream) {
        UUID id = UUID.randomUUID();
        this.dataStreams.put(id.toString(), dataStream);
        // TODO: how to dynamically get the serving ip/port from Spring?
        try {
            return new URL("http://" + "192.168.0.101" + ":" + 19161 + "/" + PATH_PREFIX + "/" + id.toString());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Exception during creation of URL", e);
        }
    }

    private class StreamAndMimeType {
        public InputStream stream;
        public String mimeType;

        public StreamAndMimeType(InputStream stream, String mimeType) {
            this.stream = stream;
            this.mimeType = mimeType;
        }
    }

}
