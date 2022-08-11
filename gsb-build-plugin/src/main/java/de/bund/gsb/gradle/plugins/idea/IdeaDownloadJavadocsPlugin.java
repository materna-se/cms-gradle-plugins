package de.bund.gsb.gradle.plugins.idea;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.plugins.ide.idea.model.IdeaModel;

public class IdeaDownloadJavadocsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPlugins().withType(JavaPlugin.class, jp -> project.getPlugins().apply(IdeaPlugin.class));

        project.getPlugins().withType(IdeaPlugin.class, ip -> {
            IdeaModel ideaModel = project.getExtensions().getByType(IdeaModel.class);
            ideaModel.getModule().setDownloadJavadoc(true);
        });
    }
}
