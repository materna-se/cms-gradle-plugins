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
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.provider.MapProperty;
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage;

import java.util.Map;

import static de.materna.cms.gradle.plugins.materna_metadata.MaternaMetadataPlugin.MATERNA_NAME;
import static de.materna.cms.gradle.plugins.materna_metadata.MaternaMetadataPlugin.MATERNA_URL;

public class SpringBootMetadataPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getTasks().withType(BootBuildImage.class).configureEach(bootBuildImage -> {

            Object environment = new DslObject(bootBuildImage).getAsDynamicObject().getProperty("environment");

            if (environment instanceof Map) { // Spring Boot 2
                Map<String, String> envMap = (Map<String, String>) environment;
                envMap.put("BP_OCI_VENDOR", MATERNA_NAME);
                envMap.put("BP_OCI_URL", MATERNA_URL);
            } else if (environment instanceof MapProperty) { // Spring Boot 3.x
                MapProperty<String, String> envMapProperty = (MapProperty<String, String>) environment;
                envMapProperty.put("BP_OCI_VENDOR", MATERNA_NAME);
                envMapProperty.put("BP_OCI_URL", MATERNA_URL);
            }
        });
    }
}
