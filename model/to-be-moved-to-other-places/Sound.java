package com.programyourhome.iotadventure.model;

import java.io.InputStream;
import java.util.function.Supplier;

public class Sound {

    public String name;
    public Supplier<InputStream> inputStream;
    public boolean cacheable;

}
