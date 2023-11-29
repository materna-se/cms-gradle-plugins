package de.materna.cms.gradle.plugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WarLibraryPluginTest {

    private Project project;

    @BeforeEach
    void init() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    void apply() {
        project.getPlugins().apply(WarLibraryPlugin.class);
    }

}