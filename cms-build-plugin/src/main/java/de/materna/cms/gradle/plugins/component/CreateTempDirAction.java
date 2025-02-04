package de.materna.cms.gradle.plugins.component;

import org.gradle.api.Action;
import org.gradle.api.Task;

import java.io.File;

public class CreateTempDirAction implements Action<Task> {
    @Override
    public void execute(Task task) {
        File temporaryDir = task.getTemporaryDir();

        if (!temporaryDir.exists()) {
            temporaryDir.mkdirs();
        }
    }
}
