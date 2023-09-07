package de.materna.cms.gradle.plugins.ci;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.api.*;
import org.gradle.api.logging.StandardOutputListener;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;

import java.io.File;
import java.io.IOException;

public class JavaLogsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {

        File buildDir = project.getBuildDir();

        project.getTasks().withType(Javadoc.class).configureEach(javadocTask -> {
            File errFile = new File(buildDir, "reports/javadoc/" + javadocTask.getName() + ".err");

            configureFileLogging(javadocTask, errFile);
        });

        project.getTasks().withType(JavaCompile.class).configureEach(javaCompile -> {
            File errFile = new File(buildDir, "reports/javac/" + javaCompile.getName() + ".err");

            configureFileLogging(javaCompile, errFile);

            javaCompile.getOptions().getCompilerArgs().add("-Xlint");
        });
    }

    @SuppressWarnings("Convert2Lambda")
    public static void configureFileLogging(Task task, File stdErrorFile) {
        task.getOutputs().files(stdErrorFile);
        task.getLogging().addStandardErrorListener(new FileStandardOutputListener(stdErrorFile));
        task.doFirst(new Action<Task>() {
            @Override
            public void execute(Task t) {
                ensureEmptyFile(stdErrorFile);
            }
        });
    }

    public static void ensureEmptyFile(File outFile) {
        File parentFile = outFile.getParentFile();
        try {
            if (parentFile.exists() || parentFile.mkdirs()) {
                ResourceGroovyMethods.setText(outFile, "");
            } else {
                throw new GradleException(parentFile + " kann nicht angelegt werden");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RequiredArgsConstructor
    public static class FileStandardOutputListener implements StandardOutputListener {

        private final File file;

        @Override
        @SneakyThrows
        public void onOutput(CharSequence charSequence) {
            ResourceGroovyMethods.append(file, charSequence);
        }
    }
}
