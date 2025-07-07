package utils;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

    private static final Properties properties;

    static {
        String filePath = "src/test/resources/config.properties";
        try {
            FileInputStream input = new FileInputStream(filePath);
            properties = new Properties();
            properties.load(input);
            input.close();
        } catch (IOException e) {
            System.err.println("ERROR: Failed to load properties file '" + filePath + "'. Details: " + e.getMessage());
            throw new RuntimeException("Error loading properties file: " + e.getMessage());
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}