/*
 * Copyright (c) 2023, Materna Information & Communications SE, Germany
 */

package de.materna.tremonia.tools.selenium;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * JUnit 5 Extension für Selenium-Tests
 */
public class SeleniumTest implements BeforeEachCallback, ParameterResolver, AfterEachCallback {

  private WebDriver createDriver() {
    ChromeOptions options = new ChromeOptions();
    if (System.getenv("JENKINS_HOME") != null || "jenkins".equals(System.getenv("USER"))) {
      options.setHeadless(true);
    }
    options.setAcceptInsecureCerts(true);
    return new ChromeDriver(options);
  }

  private ExtensionContext.Store getStore(ExtensionContext context) {
    return context.getStore(ExtensionContext.Namespace.create(SeleniumTest.class, context.getRequiredTestClass()));
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) throws ParameterResolutionException {
    return parameterContext.getParameter().getType().isAssignableFrom(ChromeDriver.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return getStore(extensionContext).get("driver", WebDriver.class);
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    getStore(context).put("driver", createDriver());
    getStore(context).get("driver", WebDriver.class).manage().window().setSize(new Dimension(1024, 768));
  }

  @Override
  public void afterEach(ExtensionContext context) {
    getStore(context).get("driver", WebDriver.class).quit();
  }

}
