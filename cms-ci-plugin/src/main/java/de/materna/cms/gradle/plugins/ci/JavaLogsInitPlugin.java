package de.materna.cms.gradle.plugins.ci;

import org.gradle.api.Plugin;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.BasePlugin;

public class JavaLogsInitPlugin implements Plugin<Gradle> {
    @Override
    public void apply(Gradle gradle) {

        gradle.allprojects(project -> {

            project.getPlugins().withType(BasePlugin.class, basePlugin -> {
                project.getPlugins().apply(JavaLogsPlugin.class);
            });

        });

    }
}
