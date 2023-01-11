package de.materna.gsb.gradle.plugins.component;


import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.distribution.Distribution;
import org.gradle.api.distribution.DistributionContainer;
import org.gradle.api.distribution.plugins.DistributionPlugin;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Tar;
import org.gradle.api.tasks.bundling.Zip;

import javax.inject.Inject;

public class GsbComponentBundlePlugin implements Plugin<Project> {

    private final SoftwareComponentFactory softwareComponentFactory;

    @Inject
    public GsbComponentBundlePlugin(SoftwareComponentFactory softwareComponentFactory) {
        this.softwareComponentFactory = softwareComponentFactory;
    }

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(DistributionPlugin.class);

        Configuration gsbComponent = GsbComponentUtil.maybeCreateGsbComponentConfiguration(project);
        Configuration gsbComponentBundle = GsbComponentUtil.maybeCreateGsbComponentBundleConfiguration(project);

        gsbComponent.setTransitive(false);

        NamedDomainObjectProvider<Distribution> mainDistribution = project.getExtensions().getByType(DistributionContainer.class).named(DistributionPlugin.MAIN_DISTRIBUTION_NAME);

        mainDistribution.configure(main -> {
            main.getContents().from(gsbComponent);
        });

        TaskProvider<Zip> distZip = project.getTasks().named("distZip", Zip.class);
        TaskProvider<Tar> distTar = project.getTasks().named("distTar", Tar.class, tar -> tar.setEnabled(false));

        project.getArtifacts().add(gsbComponentBundle.getName(), distZip);

        AdhocComponentWithVariants gsbComponentSc = softwareComponentFactory.adhoc("gsbComponentBundle");
        project.getComponents().add(gsbComponentSc);
        gsbComponentSc.addVariantsFromConfiguration(gsbComponentBundle, details -> {
        });

        project.getPlugins().withType(MavenPublishPlugin.class, mpp -> {

            PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);

            publishing.getPublications().register("gsbComponentBundle", MavenPublication.class, mavenPublication -> {
                mavenPublication.from(gsbComponentSc);
            });

        });

    }

}
