package pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.TimeoutException;

public class ValidationHelper extends BasePage {

    public ValidationHelper(AndroidDriver driver) {
        super(driver);
    }

    public void assertTextsPresent(String... textsToValidate) {
        if (textsToValidate == null || textsToValidate.length == 0) {
            System.err.println("Warning: No texts were provided for validation.");
            return;
        }
        String joinedTexts = String.join(", ", textsToValidate);
        System.out.println("Waiting for all texts (" + joinedTexts + ")");
        try {
            for (String text : textsToValidate) {
                if (text == null) {
                    System.err.println("Warning: A null text was passed for validation. Ignoring.");
                    continue;
                }
                wait.until(driver -> {
                    String pageSource = driver.getPageSource();
                    return pageSource != null && pageSource.contains(text);
                });
            }
            System.out.println("All texts were found (" + joinedTexts + ")");
        } catch (TimeoutException e) {
            String errorMessage = String.format(
                    "Validation failed: Not all texts were found (" + joinedTexts + ") after %d seconds.", 10
            );
            System.err.println(errorMessage);
            throw new AssertionError(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = String.format(
                    "Unexpected error while validating texts (" + joinedTexts + "): %s", e.getMessage()
            );
            System.err.println(errorMessage);
            throw new AssertionError(errorMessage, e);
        }
    }
}