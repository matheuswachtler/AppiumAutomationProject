package utils.tests;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import com.google.gson.Gson;

public class TestUtils {


    public static String recoverValue(String key) {
        String scriptName = null;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().contains("tests") && !element.getMethodName().equals("recoverValue")) {
                scriptName = element.getMethodName();
                break;
            }
        }
        if (scriptName == null) {
            System.err.println("Unable to identify test script name.");
            return null;
        }
        String massDir = "src/test/resources/mass";
        File foundFile = findJsonFileRecursively(new File(massDir), scriptName + ".json");
        if (foundFile == null) {
            System.err.println("JSON file not found for script: " + scriptName);
            return null;
        }
        try (FileReader reader = new FileReader(foundFile)) {
            Gson gson = new Gson();
            com.google.gson.JsonArray array = gson.fromJson(reader, com.google.gson.JsonArray.class);
            if (array.isEmpty()) return null;
            com.google.gson.JsonObject root = array.get(0).getAsJsonObject();
            com.google.gson.JsonObject scriptObj = root.getAsJsonObject(scriptName);
            if (scriptObj == null) return null;
            String platform = System.getProperty("platformName", "android").toLowerCase();
            com.google.gson.JsonObject platformObj = scriptObj.getAsJsonObject(platform);
            if (platformObj == null) return null;
            com.google.gson.JsonElement value = platformObj.get(key);
            return value != null ? value.getAsString() : null;
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
            return null;
        }
    }

    private static File findJsonFileRecursively(File dir, String fileName) {
        if (!dir.exists() || !dir.isDirectory()) return null;
        File[] files = dir.listFiles();
        if (files == null) return null;
        for (File file : files) {
            if (file.isDirectory()) {
                File found = findJsonFileRecursively(file, fileName);
                if (found != null) return found;
            } else if (file.getName().equalsIgnoreCase(fileName)) {
                return file;
            }
        }
        return null;
    }
}
