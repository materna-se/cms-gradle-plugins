package de.materna.cms.gradle.plugins.component;


import de.materna.cms.gradle.plugins.maven.MavenRepositoryPlugin;
import org.cyclonedx.gradle.CycloneDxTask;
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
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Tar;
import org.gradle.api.tasks.bundling.Zip;

import javax.inject.Inject;
import java.util.Collections;

public class CmsComponentBundlePlugin implements Plugin<Project> {

    public static final String CMS_COMPONENT_BUNDLE_PUBLICATION = "cmsComponentBundle";
    private final SoftwareComponentFactory softwareComponentFactory;

    @Inject
    public CmsComponentBundlePlugin(SoftwareComponentFactory softwareComponentFactory) {
        this.softwareComponentFactory = softwareComponentFactory;
    }

    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;
        project.getPlugins().apply(DistributionPlugin.class);

        Configuration cmsComponent = CmsComponentUtil.maybeCreateCmsComponentConfiguration(project);
        Configuration cmsComponentBundle = CmsComponentUtil.maybeCreateCmsComponentBundleConfiguration(project);

        cmsComponent.setTransitive(false);

        NamedDomainObjectProvider<Distribution> mainDistribution = project.getExtensions().getByType(DistributionContainer.class).named(DistributionPlugin.MAIN_DISTRIBUTION_NAME);

        mainDistribution.configure(main -> {
            main.getContents().into(project.getVersion(), versionFolder -> versionFolder.from(cmsComponent));
        });

        TaskProvider<Zip> distZip = project.getTasks().named("distZip", Zip.class);
        TaskProvider<Tar> distTar = project.getTasks().named("distTar", Tar.class, tar -> tar.setEnabled(false));

        distZip.configure(task -> task.getArchiveVersion().set(""));
        distTar.configure(task -> task.getArchiveVersion().set(""));

        project.getArtifacts().add(cmsComponentBundle.getName(), distZip);

        AdhocComponentWithVariants cmsComponentSc = softwareComponentFactory.adhoc("cmsComponentBundle");
        project.getComponents().add(cmsComponentSc);
        cmsComponentSc.addVariantsFromConfiguration(cmsComponentBundle, details -> {
        });

        project.getPlugins().withType(MavenPublishPlugin.class, mpp -> {

            PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);

            publishing.getPublications().register(CMS_COMPONENT_BUNDLE_PUBLICATION, MavenPublication.class, mavenPublication -> {
                mavenPublication.from(cmsComponentSc);
            });

        });

        project.getPlugins().withType(MavenRepositoryPlugin.class, mavenRepositoryPlugin -> {
            project.getTasks().withType(PublishToMavenRepository.class, publish -> {
                publish.onlyIf(t -> !(publish.getRepository() == mavenRepositoryPlugin.getMavenRepository() && publish.getPublication().getName().equals(CMS_COMPONENT_BUNDLE_PUBLICATION)));
            });
        });

        project.getPlugins().withId("org.cyclonedx.bom", this::configureCycloneDx);
    }

    private void configureCycloneDx(Plugin<?> plugin) {
        TaskProvider<CycloneDxTask> cyclonedxBomTask = project.getTasks().named("cyclonedxBom", CycloneDxTask.class);

        //Erst aus machen, und dann für War und Application wieder an machen.
        cyclonedxBomTask.configure(cyclonedxBom -> {
            cyclonedxBom.getIncludeConfigs().convention(Collections.singletonList("cmsComponent"));
            cyclonedxBom.getProjectType().convention("application");
        });
    }

}
