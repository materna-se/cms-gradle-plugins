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

import com.google.cloud.tools.jib.gradle.ContainerParameters;
import com.google.cloud.tools.jib.gradle.JibExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static de.materna.cms.gradle.plugins.materna_metadata.MaternaMetadataPlugin.MATERNA_NAME;
import static de.materna.cms.gradle.plugins.materna_metadata.MaternaMetadataPlugin.MATERNA_URL;

public class JibMetadataPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        JibExtension jib = project.getExtensions().getByType(JibExtension.class);

        ContainerParameters container = jib.getContainer();

        container.getLabels().put("org.label-schema.schema-version", "1.0");

        container.getLabels().put("maintainer", MATERNA_NAME);
        container.getLabels().put("org.label-schema.vendor", MATERNA_NAME);
        container.getLabels().put("org.opencontainers.image.vendor", MATERNA_NAME);

        container.getLabels().put("org.label-schema.url", MATERNA_URL);
        container.getLabels().put("org.opencontainers.image.url", MATERNA_URL);
    }
}
