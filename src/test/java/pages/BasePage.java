package pages;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.OutputType;
import utils.PdfReporter;
import utils.DriverManager;

import java.time.Duration;

public class BasePage {

    protected AndroidDriver driver;
    protected WebDriverWait wait;
    protected PdfReporter pdfReporter;

    public BasePage(AndroidDriver driver) {
        this.driver = driver;
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ofSeconds(10)), this);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.pdfReporter = DriverManager.getPdfReporter();
    }

    public WebElement waitForElementVisibility(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    public void enterText(WebElement element, String text, String errorMessage) {
        try {
            WebElement visibleElement = waitForElementVisibility(element);
            visibleElement.sendKeys(text);
        } catch (TimeoutException e) {
            System.err.println("Timeout Error: " + errorMessage + " - " + e.getMessage());
            throw new RuntimeException(errorMessage, e);
        } catch (Exception e) {
            System.err.println("Error sending text: " + errorMessage + " - " + e.getMessage());
            throw new RuntimeException(errorMessage, e);
        }
    }

    public void clickElement(WebElement element, String errorMessage) {
        try {
            WebElement clickableElement = wait.until(ExpectedConditions.elementToBeClickable(element));
            clickableElement.click();
        } catch (TimeoutException e) {
            System.err.println("Timeout Error: " + errorMessage + " - " + e.getMessage());
            throw new RuntimeException(errorMessage, e);
        } catch (Exception e) {
            System.err.println("Error clicking: " + errorMessage + " - " + e.getMessage());
            throw new RuntimeException(errorMessage, e);
        }
    }

    public void saveEvidence(String name) {
        if (driver == null) {
            System.err.println("Driver is null. Cannot capture screenshot for evidence.");
            return;
        }
        if (pdfReporter == null) {
            System.err.println("PdfReporter is null. Cannot save evidence to PDF. Ensure PdfReporter is initialized in HooksManager.");
            return;
        }
        try {
            byte[] screenshotBytes = driver.getScreenshotAs(OutputType.BYTES);
            pdfReporter.addScreenshot(screenshotBytes, name);
        } catch (Exception e) {
            System.err.println("Error capturing screenshot for evidence: " + e.getMessage());
        }
    }
}
