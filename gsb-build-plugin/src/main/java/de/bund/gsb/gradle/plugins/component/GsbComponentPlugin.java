package de.bund.gsb.gradle.plugins.component;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.distribution.Distribution;
import org.gradle.api.distribution.DistributionContainer;
import org.gradle.api.distribution.plugins.DistributionPlugin;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.application.CreateStartScripts;
import org.gradle.api.tasks.bundling.War;
import org.springframework.boot.gradle.tasks.bundling.BootWar;

public class GsbComponentPlugin implements Plugin<Project> {

    private Project project;
    private Distribution mainDistribution;
    private GsbComponentExtension extension;

    @Override
    public void apply(Project project) {
        this.project = project;
        ObjectFactory objectFactory = project.getObjects();
        extension = project.getExtensions().create("gsbComponent", GsbComponentExtension.class);

        extension.getName().convention(project.getName());


        project.getPlugins().apply(DistributionPlugin.class);

        DistributionContainer distributions = project.getExtensions().getByType(DistributionContainer.class);
        mainDistribution = distributions.getByName("main");

        mainDistribution.getDistributionBaseName().set(extension.getName());

        Configuration configuration = project.getConfigurations().create("gsbComponents");
        configuration.getAttributes().attribute(Bundling.BUNDLING_ATTRIBUTE, objectFactory.named(Bundling.class, Bundling.EXTERNAL));
        configuration.getAttributes().attribute(Category.CATEGORY_ATTRIBUTE, objectFactory.named(Category.class, "gsb-component"));

        project.getArtifacts().add(configuration.getName(), project.getTasks().named("distZip"));

        project.getComponents().all(softwareComponent -> {
            project.getLogger().warn("{} -> {} : {}", softwareComponent.getName(), softwareComponent.getClass(), softwareComponent);

            if(softwareComponent instanceof AdhocComponentWithVariants) {
                ((AdhocComponentWithVariants) softwareComponent).addVariantsFromConfiguration(configuration, action -> {

                });
            }
        });

        project.getPlugins().withType(WarPlugin.class, this::configureWarComponent);
    }

    void configureWarComponent(WarPlugin warPlugin) {
        Property<War> warTask = project.getObjects().property(War.class);

        warTask.convention(project.getTasks().named("war", War.class));

        project.getPlugins().withId("org.springframework.boot", sbp -> {
            TaskProvider<BootWar> bootWar = project.getTasks().named("bootWar", BootWar.class);
            warTask.set(bootWar);

            TaskProvider<CreateStartScripts> css = project.getTasks().register(ApplicationPlugin.TASK_START_SCRIPTS_NAME, CreateStartScripts.class, startScripts -> {
                startScripts.getMainClass().set("org.springframework.boot.loader.WarLauncher");
                startScripts.setOutputDir(project.getLayout().getBuildDirectory().dir("gsbScripts").get().getAsFile());
                startScripts.setApplicationName(extension.getName().get());
            });

            mainDistribution.contents(dist -> {
                dist.into("bin", binSpec -> {
                    binSpec.from(css);
                });
            });

        });

        project.afterEvaluate(p -> {
            p.getTasks().getByName("installDist").dependsOn(warTask);
            p.getTasks().getByName("distZip").dependsOn(warTask);
            p.getTasks().getByName("distTar").dependsOn(warTask);
            mainDistribution.contents(distContent -> {
                distContent.from(p.zipTree(warTask.flatMap(War::getArchiveFile)));
            });
        });
    }
}
