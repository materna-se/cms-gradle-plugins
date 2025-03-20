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
