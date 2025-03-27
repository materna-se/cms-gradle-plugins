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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage;

import static de.materna.cms.gradle.plugins.materna_metadata.MaternaMetadataPlugin.MATERNA_NAME;
import static de.materna.cms.gradle.plugins.materna_metadata.MaternaMetadataPlugin.MATERNA_URL;

public class SpringBootMetadataPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getTasks().withType(BootBuildImage.class).configureEach(bootBuildImage -> {
            bootBuildImage.environment("BP_OCI_VENDOR", MATERNA_NAME);
            bootBuildImage.environment("BP_OCI_URL", MATERNA_URL);
        });
    }
}
