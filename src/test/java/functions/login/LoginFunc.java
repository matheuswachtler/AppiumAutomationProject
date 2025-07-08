package functions.login;

import constants.login.MessagesAndTitlesLogin;
import io.appium.java_client.android.AndroidDriver;
import pages.ValidationHelper;
import pages.login.LoginPage;

public class LoginFunc {

    private final AndroidDriver driver;

    public LoginFunc(AndroidDriver driver) {
        this.driver = driver;
    }

    public void validatesAccessSuccessfully(String username, String password) {

        new ValidationHelper(driver)
                .assertTextsPresent(MessagesAndTitlesLogin.LOGIN_PAGE);
        new LoginPage(driver)
                .insertCredentials(username, password)
                .clickLoginButton();
    }

}
