package tests.login;

import functions.login.LoginFunc;
import jdk.jfr.Description;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import utils.DriverManager;
import utils.HooksManager;

@ExtendWith(HooksManager.class)
public class QA123456IT {

    @Test
    @DisplayName("QA123456 - Validates access to the application with valid credentials")
    @Tag("login")
    @Description(
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry.\n" +
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry.\n" +
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry.\n" +
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry.\n" +
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry.\n" +
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry.\n" +
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry.\n" +
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry.\n" +
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry.\n" +
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry.\n" )

    public void QA123456() throws Exception {
        LoginFunc login = new LoginFunc(DriverManager.getDriver());

        login.validatesAccessSuccessfully(
                "standard_user",
                "secret_sauce");
    }
}