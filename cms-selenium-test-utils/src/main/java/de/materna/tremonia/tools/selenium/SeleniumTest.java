/*
 * Copyright 2023-2025 Materna Information & Communications SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
@Deprecated
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
