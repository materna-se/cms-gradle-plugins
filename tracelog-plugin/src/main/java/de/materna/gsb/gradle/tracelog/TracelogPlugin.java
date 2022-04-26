package de.materna.gsb.gradle.tracelog;

import io.freefair.gradle.plugins.aspectj.AspectJPostCompileWeavingPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;

public class TracelogPlugin implements Plugin<Project> {

    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;

        project.getPlugins().withType(JavaPlugin.class, this::execute);
    }

    private void execute(JavaPlugin plugin) {
        project.getPlugins().apply(AspectJPostCompileWeavingPlugin.class);

        project.getDependencies().add("aspect", "de.materna.gsb.tools:tracelog-aspect:" + this.getClass().getPackage().getImplementationVersion());
    }
}
