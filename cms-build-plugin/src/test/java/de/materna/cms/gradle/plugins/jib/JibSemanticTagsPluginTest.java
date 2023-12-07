package de.materna.cms.gradle.plugins.jib;

import com.google.cloud.tools.jib.gradle.JibExtension;
import com.google.cloud.tools.jib.gradle.JibPlugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


class JibSemanticTagsPluginTest {

    @Test
    void apply() {
        Project project = ProjectBuilder.builder().build();

        project.setVersion("1.2.3.4");

        project.getPlugins().apply(JibPlugin.class);
        project.getPlugins().apply(JibSemanticTagsPlugin.class);


        Set<String> tags = project.getExtensions().getByType(JibExtension.class).getTo().getTags();

        assertThat(tags).contains("1", "1.2", "1.2.3", "1.2.3.4");
    }

    @Test
    void apply_snapshot() {
        Project project = ProjectBuilder.builder().build();

        project.setVersion("1.2.x-SNAPSHOT");

        project.getPlugins().apply(JibPlugin.class);
        project.getPlugins().apply(JibSemanticTagsPlugin.class);

        Set<String> tags = project.getExtensions().getByType(JibExtension.class).getTo().getTags();

        assertThat(tags).containsExactly("1.2.x-SNAPSHOT");
    }

    @Test
    void resolveTags() {
        Set<String> tags = JibSemanticTagsPlugin.resolveTags("24.1.2.3");

        assertThat(tags).contains("24", "24.1", "24.1.2");
    }
}