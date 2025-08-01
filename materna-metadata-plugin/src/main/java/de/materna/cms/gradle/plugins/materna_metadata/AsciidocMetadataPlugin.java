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

import org.asciidoctor.gradle.jvm.AsciidoctorJExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.time.Year;

public class AsciidocMetadataPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {

        AsciidoctorJExtension asciidoctorJExtension = project.getExtensions().getByType(AsciidoctorJExtension.class);

        asciidoctorJExtension.attribute("author", MaternaMetadataPlugin.MATERNA_NAME);
        asciidoctorJExtension.attribute("orgname", MaternaMetadataPlugin.MATERNA_NAME);
        asciidoctorJExtension.attribute("copyright", String.format("© %s %s", Year.now(), MaternaMetadataPlugin.MATERNA_NAME));
    }
}
