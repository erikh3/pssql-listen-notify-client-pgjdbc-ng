package cz.fi.muni.pa036.listennotify.client.ng;

import lombok.extern.java.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class provides basic support for configuration properties
 *
 * @author Erik Horváth
 */
@Log
public class ConfigProperties {
    private static final String EXTERNAL_CONFIG_FILE = "./application.properties";

    private static Properties props = new Properties();

    private ConfigProperties() {
    }

    static {
        try (InputStream input = ConfigProperties.class.getResourceAsStream("/application.properties")) {
            props.load(input);
        } catch (IOException e) {
            log.severe("Cannot load default configuration from classpath");
        }

        try (InputStream input = new FileInputStream(EXTERNAL_CONFIG_FILE)) {
            props.load(input);
        } catch (IOException ex) {
            log.info(EXTERNAL_CONFIG_FILE + " cannot be read, using defaults");
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}
