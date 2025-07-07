package utils;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import utils.report.PdfReporter;

import java.net.URL;

public class DriverManager {

    private static AndroidDriver driver;
    private static PdfReporter currentPdfReporter;

    public static AndroidDriver getDriver() {
        if (driver == null) {
            throw new IllegalStateException("Appium driver is not initialized. Ensure HooksManager is properly configured and running.");
        }
        return driver;
    }

    public static void initializeDriver(PdfReporter pdfReporter) {
        if (driver != null) {
            System.out.println("Driver already initialized. Skipping re-initialization.");
            return;
        }
        if (pdfReporter == null) {
            throw new IllegalArgumentException("PdfReporter cannot be null during driver initialization. It must be provided by HooksManager.");
        }

        currentPdfReporter = pdfReporter;

        String appiumServerURL = ConfigReader.getProperty("appium.server.url");
        UiAutomator2Options options = new UiAutomator2Options();

        options.setPlatformName(ConfigReader.getProperty("platform.name"));
        options.setDeviceName(ConfigReader.getProperty("device.name"));
        options.setAutomationName("UiAutomator2");
        options.setNewCommandTimeout(java.time.Duration.ofSeconds(300));
        options.setApp(ConfigReader.getProperty("app.path"));
        options.setAppPackage(ConfigReader.getProperty("app.package"));
        options.setAppActivity(ConfigReader.getProperty("app.activity"));

        boolean noReset = Boolean.parseBoolean(ConfigReader.getProperty("no.reset"));
        options.setNoReset(noReset);

        try {
            driver = new AndroidDriver(new URL(appiumServerURL), options);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing driver: ", e);
        }
    }

    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
        currentPdfReporter = null;
    }

    public static PdfReporter getPdfReporter() {
        return currentPdfReporter;
    }
}
