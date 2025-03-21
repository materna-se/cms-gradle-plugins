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

package de.materna.cms.gradle.plugins.trivy;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.distribution.plugins.DistributionPlugin;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskProvider;

public class TrivyPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getPlugins().withType(DistributionPlugin.class, distributionPlugin -> {
            this.configureDistribution(project);
        });

        project.getTasks().withType(TrivyFilesystem.class).configureEach(trivyFilesystem -> {
            trivyFilesystem.getOfflineScan().convention(project.getGradle().getStartParameter().isOffline());
            trivyFilesystem.getFormat().convention("cyclonedx");
        });

    }

    private void configureDistribution(Project project) {

        TaskProvider<Sync> installDist = project.getTasks().named("installDist", Sync.class);

        project.getTasks().register("trivy", TrivyFilesystem.class, trivy -> {
            trivy.dependsOn(installDist);
            trivy.getSourceDirectory().set(installDist.get().getDestinationDir());
        });

    }
}
