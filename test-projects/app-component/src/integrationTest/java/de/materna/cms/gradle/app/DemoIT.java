package de.materna.cms.gradle.app;

import de.materna.tremonia.tools.selenium.SeleniumTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SeleniumTest.class)
public class DemoIT {

    @Test
    void login(WebDriver webDriver) {
        webDriver.get("http://docs.gsb.dev.materna.net/cms-gradle-plugin/");

        assertThat(webDriver.getCurrentUrl()).contains("/cms-gradle-plugin");

        assertThat(webDriver.findElements(By.cssSelector("h1")))
                .anyMatch(webElement -> webElement.getText().contains("Index of"));
    }
}
