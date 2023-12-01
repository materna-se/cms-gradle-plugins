package de.materna.cms.gradle.plugins.ci;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.api.*;
import org.gradle.api.file.RegularFile;
import org.gradle.api.logging.StandardOutputListener;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

/**
 * Dieses Plugin erweitert die {@link JavaCompile}- und {@link Javadoc}-Tasks um eine Output-File
 * mit allen lint Warnungen, damit diese vom Jenkins ausgelesen werden können.
 * <p>
 * In der Ausgabe werden durch Ersetzung des {@link JavaLogsExtension#getBaseDir() baseDir} relative Pfade erzeugt,
 * damit die Tasks cachebar bleiben.
 *
 * @see JavaLogsExtension
 */
public class JavaLogsPlugin implements Plugin<Project> {

    private JavaLogsExtension extension;

    @Override
    public void apply(@Nonnull Project project) {
        extension = project.getExtensions().create("javaLogs", JavaLogsExtension.class);

        resolveBaseDir(project);

        project.allprojects(subproject -> {
            subproject.getTasks().withType(Javadoc.class).configureEach(javadocTask -> {
                Provider<RegularFile> errFile = javadocTask.getProject().getLayout().getBuildDirectory().file("reports/javadoc/" + javadocTask.getName() + ".err");

                configureFileLogging(javadocTask, errFile.get());
            });

            subproject.getTasks().withType(JavaCompile.class).configureEach(javaCompile -> {
                Provider<RegularFile> errFile = javaCompile.getProject().getLayout().getBuildDirectory().file("reports/javac/" + javaCompile.getName() + ".err");

                configureFileLogging(javaCompile, errFile.get());

                javaCompile.getOptions().getCompilerArgs().add("-Xlint");
            });
        });
    }

    private void resolveBaseDir(Project project) {
        Provider<String> workspace = project.getProviders().environmentVariable("WORKSPACE");

        Provider<String> gitToplevel = project.getProviders()
                .exec(execSpec -> {
                    execSpec.commandLine("git", "rev-parse", "--show-toplevel");
                    execSpec.setIgnoreExitValue(true);
                })
                .getStandardOutput()
                .getAsText()
                .map(String::trim);

        if (workspace.isPresent()) {
            extension.getBaseDir().convention(workspace);
        } else if (gitToplevel.isPresent()) {
            extension.getBaseDir().convention(gitToplevel);
        } else {
            project.getLogger().warn("Konnte Workspace nicht bestimmen");
        }
    }

    @SuppressWarnings("Convert2Lambda")
    public void configureFileLogging(Task task, RegularFile stdErrorFile) {
        task.getOutputs().files(stdErrorFile);
        task.getLogging().addStandardErrorListener(new FileStandardOutputListener(stdErrorFile.getAsFile()));
        task.doFirst(new Action<Task>() {
            @Override
            public void execute(Task t) {
                ensureEmptyFile(stdErrorFile.getAsFile());
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
    public class FileStandardOutputListener implements StandardOutputListener {

        private final File file;

        @Override
        @SneakyThrows
        public void onOutput(CharSequence charSequence) {
            if (charSequence != null && extension.getBaseDir().isPresent()) {
                charSequence = charSequence.toString().replace(extension.getBaseDir().get(), ".");
            }
            ResourceGroovyMethods.append(file, charSequence);
        }
    }
}
