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
