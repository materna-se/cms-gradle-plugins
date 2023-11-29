package de.materna.cms.gradle.plugins.maven;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MavenRepositoryPluginTest {

    private Project project;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    void apply() {
        project.getPlugins().apply(MavenRepositoryPlugin.class);
    }
}