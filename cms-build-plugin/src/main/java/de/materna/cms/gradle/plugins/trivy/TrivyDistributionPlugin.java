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

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.distribution.Distribution;
import org.gradle.api.distribution.DistributionContainer;
import org.gradle.api.distribution.plugins.DistributionBasePlugin;
import org.gradle.api.distribution.plugins.DistributionPlugin;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskProvider;

public class TrivyDistributionPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {

        project.getPlugins().apply(DistributionBasePlugin.class);

        DistributionContainer distributions = project.getExtensions().getByType(DistributionContainer.class);

        distributions.configureEach(distribution -> {
            TaskProvider<Sync> installDist = project.getTasks().named(getInstallDistTaskName(distribution), Sync.class);

            project.getTasks().register(getTrivyTaskName(distribution), TrivyRootfs.class, trivy -> {
                trivy.dependsOn(installDist);
                trivy.getSourceDirectory().set(installDist.get().getDestinationDir());
            });

        });
    }

    private String getInstallDistTaskName(Distribution distribution) {
        if (distribution.getName().equals("main")) {
            return DistributionPlugin.TASK_INSTALL_NAME;
        } else {
            return "install" + StringUtils.capitalize(distribution.getName()) + "Dist";
        }
    }

    private String getTrivyTaskName(Distribution distribution) {
        if (distribution.getName().equals("main")) {
            return "trivy";
        } else {
            return distribution.getName() + "Trivy";
        }
    }
}
