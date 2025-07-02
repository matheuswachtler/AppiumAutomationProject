package pages.login;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;
import pages.BasePage;

public class LoginPage extends BasePage {

    @AndroidFindBy(xpath = "//*[contains(@content-desc, 'Username')]")
    private WebElement usernameField;

    @AndroidFindBy(xpath = "//*[contains(@content-desc, 'Password')]")
    private WebElement passwordField;

    @AndroidFindBy(xpath = "//*[contains(@content-desc, 'LOGIN')]")
    private WebElement loginButton;

    public LoginPage(AndroidDriver driver) {
        super(driver);
    }

    public LoginPage insertCredentials(String username, String password) {
        saveEvidence("Fill in credentials fields");
        enterText(usernameField,username,"Error entering username");
        enterText(passwordField,password,"Error entering password");
        saveEvidence("Credentials successfully completed");
        return this;
    }


    public LoginPage clickLoginButton() {
        saveEvidence("Click login button");
        clickElement(loginButton, "Error clicking login button");
        return this;
    }

}
