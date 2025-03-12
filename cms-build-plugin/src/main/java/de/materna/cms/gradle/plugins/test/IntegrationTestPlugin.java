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

package de.materna.cms.gradle.plugins.test;

import lombok.Getter;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JvmTestSuitePlugin;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.tasks.SourceSet;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.testing.base.TestingExtension;

/**
 * Gradle-Plugin für Integrations-Tests.
 *
 * <p>Dieses Plugin fügt dem Projekt das {@code integrationTest}-{@link SourceSet} hinzu und konfiguriert dieses.
 */
@Getter
public class IntegrationTestPlugin implements Plugin<Project> {


    private NamedDomainObjectProvider<JvmTestSuite> integrationTestSuite;

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(JvmTestSuitePlugin.class);

        TestingExtension testingExtension = project.getExtensions().getByType(TestingExtension.class);

        integrationTestSuite = testingExtension.getSuites().register("integrationTest", JvmTestSuite.class, suite -> {
            suite.getDependencies().getImplementation().add(suite.getDependencies().project());

            suite.getTargets().all(target -> target.getTestTask().configure(test -> {
                test.setIgnoreFailures(true);
            }));
        });

        project.getTasks().named(LifecycleBasePlugin.CHECK_TASK_NAME, check -> check.dependsOn(integrationTestSuite));
    }
}

