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

package de.materna.cms.gradle.plugins.materna_metadata;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.gradle.plugin.SpringBootPlugin;
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage;

import static org.assertj.core.api.Assertions.assertThat;

class SpringBootMetadataPluginTest {

    @Test
    void testPlugin() {

        Project project = ProjectBuilder.builder().build();

        project.getPlugins().apply(SpringBootPlugin.class);
        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(SpringBootMetadataPlugin.class);

        TaskProvider<BootBuildImage> bootBuildImage = project.getTasks().named("bootBuildImage", BootBuildImage.class);

        assertThat(bootBuildImage.isPresent()).isTrue();
        assertThat(bootBuildImage.get().getEnvironment().get()).containsKey("BP_OCI_VENDOR");
        assertThat(bootBuildImage.get().getEnvironment().get()).containsKey("BP_OCI_URL");

    }

}