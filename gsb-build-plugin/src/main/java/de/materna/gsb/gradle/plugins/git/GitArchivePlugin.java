package de.materna.gsb.gradle.plugins.git;

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
