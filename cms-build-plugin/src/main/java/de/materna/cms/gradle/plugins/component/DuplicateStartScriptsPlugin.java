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

package de.materna.cms.gradle.plugins.component;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.file.FileOperations;
import org.gradle.api.tasks.application.CreateStartScripts;

import javax.inject.Inject;
import java.io.File;

public abstract class DuplicateStartScriptsPlugin implements Plugin<Project> {

    @Inject
    public abstract FileOperations getFileOperations();

    @Override
    public void apply(Project project) {

        project.getTasks().withType(CreateStartScripts.class).configureEach(createStartScripts -> {
            createStartScripts.doLast(task -> {
                File unixScript = createStartScripts.getUnixScript();

                if (unixScript.isFile()) {
                    getFileOperations().copy(copySpec -> {
                        copySpec.from(unixScript);
                        copySpec.into(unixScript.getParentFile());
                        copySpec.rename(s -> "app");
                    });
                }

            });
        });

    }
}
