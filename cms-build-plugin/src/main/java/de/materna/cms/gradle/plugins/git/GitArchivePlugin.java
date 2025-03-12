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

package de.materna.cms.gradle.plugins.git;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.BasePluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.TaskProvider;

import java.io.IOException;

public class GitArchivePlugin implements Plugin<Project> {

    private TaskProvider<Exec> exportTask;
    private Provider<RegularFile> exportFile;

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(BasePlugin.class);

        BasePluginExtension basePluginExtension = project.getExtensions().getByType(BasePluginExtension.class);

        exportFile = basePluginExtension.getDistsDirectory().file(
                project.provider(() ->
                        String.format("%s-%s-git.zip", basePluginExtension.getArchivesName().get(), project.getVersion())
                )
        );

        exportTask = project.getTasks().register("gitArchive", Exec.class, exec -> {
            exec.executable("git");

            try {
                exec.getInputs().property("tree-ish", GitUtil.getCurrentGitHash());
                exec.getInputs().property("is-dirty", GitUtil.isGitDirty());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        project.afterEvaluate(p -> {
            exportTask.configure(exec -> {
                exec.args("archive", "--format=zip", "--output=" + exportFile.get().getAsFile().getAbsolutePath(), "-9", "HEAD");
                exec.getOutputs().file(exportFile);
            });
        });
    }
}
