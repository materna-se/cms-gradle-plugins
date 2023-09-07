package de.materna.cms.gradle.plugins.ci;

import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.BasePlugin;

public class JavaLogsSettingsPlugin implements Plugin<Settings> {
    @Override
    public void apply(Settings settings) {
        settings.getGradle().allprojects(project -> {
                    project.getPlugins().apply(JavaLogsPlugin.class);
                }
        );
    }
}
