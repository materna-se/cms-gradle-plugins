/*
 * Copyright 2026 Materna Information & Communications SE
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

package de.materna.cms.gradle.plugins.trivy;

import org.gradle.api.Project;
import org.gradle.api.distribution.plugins.DistributionPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrivyPluginTest {

    Project project;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    void apply() {
        project.getPlugins().apply(TrivyPlugin.class);
    }

    @Test
    void apply_trivy_distribution() {
        project.getPlugins().apply(TrivyPlugin.class);
        project.getPlugins().apply(DistributionPlugin.class);
    }

    @Test
    void apply_distribution_trivy() {
        project.getPlugins().apply(DistributionPlugin.class);
        project.getPlugins().apply(TrivyPlugin.class);
    }

}