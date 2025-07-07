package pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.TimeoutException;

public class ValidationHelper extends BasePage {

    public ValidationHelper(AndroidDriver driver) {
        super(driver);
    }

    public void assertTextsPresent(String... textsToValidate) {
        for (String text : textsToValidate) {
            if (text == null) {
                System.err.println("Warning: A null text was passed for validation. Ignoring.");
                continue;
            }

            try {
                System.out.println("Waiting for text '" + text + "' to appear on the screen...");
                wait.until(driver -> {
                    String pageSource = driver.getPageSource();
                    return pageSource != null && pageSource.contains(text);
                });
                System.out.println("Validation successful: Text '" + text + "' was found on the screen.");
            } catch (TimeoutException e) {
                String errorMessage = String.format(
                        "Validation failed: Text '%s' was not found on the screen after %d seconds.",
                        text, 10
                );
                System.err.println(errorMessage);
                throw new AssertionError(errorMessage, e);
            } catch (Exception e) {
                String errorMessage = String.format(
                        "Unexpected error while validating text '%s': %s", text, e.getMessage()
                );
                System.err.println(errorMessage);
                throw new AssertionError(errorMessage, e);
            }
        }
        System.out.println("Validation successful: All expected texts were found.");
    }
}