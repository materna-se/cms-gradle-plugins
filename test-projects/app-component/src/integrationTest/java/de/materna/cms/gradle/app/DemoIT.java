/*
 * Copyright 2025 Materna Information & Communications SE
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
