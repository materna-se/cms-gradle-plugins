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

package de.materna.cms.gradle.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

/**
 * War-Variante des {@link JavaLibraryPlugin java-library} Plugins.
 * Fügt dem Projekt die "providedApi" Configuration hinzu.
 */
public class WarLibraryPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getPlugins().apply(WarPlugin.class);
        project.getPlugins().apply(JavaLibraryPlugin.class);

        Configuration providedApi = project.getConfigurations().create("providedApi");

        SourceSet main = project.getExtensions().getByType(SourceSetContainer.class).getByName(SourceSet.MAIN_SOURCE_SET_NAME);

        Configuration providedCompile = project.getConfigurations().getByName(WarPlugin.PROVIDED_COMPILE_CONFIGURATION_NAME);
        Configuration api = project.getConfigurations().getByName(main.getApiConfigurationName());

        api.extendsFrom(providedApi);
        providedCompile.extendsFrom(providedApi);
    }
}
