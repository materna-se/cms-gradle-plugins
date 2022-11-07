package de.bund.gsb.gradle.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.WarPlugin;

public class WarLibraryPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getPlugins().apply(WarPlugin.class);
        project.getPlugins().apply(JavaLibraryPlugin.class);

        Configuration providedApi = project.getConfigurations().create("providedApi");
        Configuration providedCompile = project.getConfigurations().getByName(WarPlugin.PROVIDED_COMPILE_CONFIGURATION_NAME);
        Configuration api = project.getConfigurations().getByName("api");

        api.extendsFrom(providedApi);
        providedCompile.extendsFrom(providedApi);
    }
}
