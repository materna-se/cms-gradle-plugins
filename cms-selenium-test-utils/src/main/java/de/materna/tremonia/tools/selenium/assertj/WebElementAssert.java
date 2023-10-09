/*
 * Copyright (c) 2023, Materna Information & Communications SE, Germany
 */

package de.materna.tremonia.tools.selenium.assertj;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.WebElement;

import java.util.Objects;

public class WebElementAssert extends AbstractAssert<WebElementAssert, WebElement> {

  public WebElementAssert(WebElement actual) {
    super(actual, WebElementAssert.class);
  }

  public static WebElementAssert assertThat(WebElement actual) {
    return new WebElementAssert(actual);
  }

  public WebElementAssert isDisplayed() {
    isNotNull();

    if (!actual.isDisplayed()) {
      failWithMessage("Expected element to be displayed, but it was not: <%s>", actual);
    }

    return this;
  }

  public WebElementAssert isSelected() {
    isNotNull();

    if (!actual.isSelected()) {
      failWithMessage("Expected element to be selected, but it was not: <%s>", actual);
    }

    return this;
  }

  public WebElementAssert isEnabled() {
    isNotNull();

    if (!actual.isEnabled()) {
      failWithMessage("Expected element to be enabled, but it was not: <%s>", actual);
    }

    return this;
  }

  public WebElementAssert isTag(String tagName) {
    isNotNull();

    if (!Objects.equals(actual.getTagName(), tagName)) {
      failWithMessage("Expected element to be <%s>, but was <%s>", tagName, actual.getTagName());
    }

    return this;
  }

  public AbstractStringAssert<?> getText() {
    isNotNull();

    return Assertions.assertThat(actual.getText()).as("WebElement <%s>", actual);
  }
}
