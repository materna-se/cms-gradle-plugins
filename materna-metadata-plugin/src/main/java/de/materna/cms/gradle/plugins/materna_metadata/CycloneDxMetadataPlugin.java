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

import org.cyclonedx.gradle.CycloneDxTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Collections;

public class CycloneDxMetadataPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getTasks().withType(CycloneDxTask.class)
                .configureEach(cyclonedxBom -> cyclonedxBom.setOrganizationalEntity(organizationalEntity -> {
                    organizationalEntity.setName("Materna Information & Communications SE");
                    organizationalEntity.setUrls(Collections.singletonList("https://materna.de"));
                }));
    }
}
