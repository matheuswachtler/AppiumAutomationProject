package tests.login;

import functions.login.LoginFunc;
import jdk.jfr.Description;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import utils.DriverManager;
import utils.HooksManager;

public class QA123456IT extends HooksManager {

    @Test
    @DisplayName("QA123456 - Validates access to the application with valid credentials")
    @Tag("login")
    @Description("As a Sauce Labs user," +
            "I want to access the system with my valid credentials,\n" +
            "so that I can use the functionalities available after logging in.")
    public void QA123456() throws Exception {
        LoginFunc login = new LoginFunc(DriverManager.getDriver());

        login.validatesAccessSuccessfully(
                "standard_user",
                "secret_sauce");
    }
}
