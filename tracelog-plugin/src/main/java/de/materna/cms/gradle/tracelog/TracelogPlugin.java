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

package de.materna.cms.gradle.tracelog;

import io.freefair.gradle.plugins.aspectj.AspectJPostCompileWeavingPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;

public class TracelogPlugin implements Plugin<Project> {

    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;

        project.getPlugins().withType(JavaPlugin.class, this::execute);
    }

    private void execute(JavaPlugin plugin) {
        project.getPlugins().apply(AspectJPostCompileWeavingPlugin.class);

        project.getDependencies().add("aspect", "de.materna.cms.tools:tracelog-aspect:" + this.getClass().getPackage().getImplementationVersion());
    }
}
