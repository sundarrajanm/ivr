package com.experiment.ivr.usecase;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Utils {
    // Stop gap until we talk to core module.
    public static String readTestXML(String name) {
        try {
            return new String(Files.readAllBytes(Paths.get(
                    ClassLoader.getSystemResource(name).toURI()
            )));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return "";
    }
}
