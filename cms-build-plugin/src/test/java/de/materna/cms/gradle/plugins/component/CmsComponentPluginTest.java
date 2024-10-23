package de.materna.cms.gradle.plugins.component;

import org.gradle.api.Project;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CmsComponentPluginTest {

    private Project project;

    @BeforeEach
    void setUp() {
        this.project = ProjectBuilder.builder().build();
    }

    @Test
    void apply() {
        project.getPlugins().apply(CmsComponentPlugin.class);
    }

    @Test
    void apply_application() {
        project.getPlugins().apply(CmsComponentPlugin.class);
        project.getPlugins().apply(ApplicationPlugin.class);
    }

    @Test
    void apply_war() {
        project.getPlugins().apply(CmsComponentPlugin.class);
        project.getPlugins().apply(WarPlugin.class);
    }
}