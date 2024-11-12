package de.materna.cms.gradle.plugins.sbom;

import de.materna.cms.gradle.plugins.Util;
import org.cyclonedx.gradle.CycloneDxPlugin;
import org.cyclonedx.gradle.CycloneDxTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.Usage;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.BasePluginExtension;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.ReportingBasePlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.reporting.ReportingExtension;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;

public class SbomPlugin implements Plugin<Project> {

    public static final String SBOM_CONFIGURATION = "sbom";

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(ReportingBasePlugin.class);
        project.getPlugins().apply(CycloneDxPlugin.class);

        Configuration sbomConfiguration = project.getConfigurations().maybeCreate(SBOM_CONFIGURATION);
        sbomConfiguration.setCanBeResolved(false);
        sbomConfiguration.setCanBeConsumed(true);

        sbomConfiguration.getAttributes().attribute(Category.CATEGORY_ATTRIBUTE, project.getObjects().named(Category.class, "sbom"));
        sbomConfiguration.getAttributes().attribute(Bundling.BUNDLING_ATTRIBUTE, project.getObjects().named(Bundling.class, Bundling.EMBEDDED));
        sbomConfiguration.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.VERSION_CATALOG));

        TaskProvider<CycloneDxTask> cyclonedxBom = project.getTasks().named("cyclonedxBom", CycloneDxTask.class, cycloneDxTask -> {
            cycloneDxTask.getSchemaVersion().convention("1.6");
        });

        project.getPlugins().withType(BasePlugin.class, basePlugin -> {
            BasePluginExtension base = project.getExtensions().getByType(BasePluginExtension.class);

            cyclonedxBom.configure(cdxBom -> {
                cdxBom.getOutputName().convention(base.getArchivesName().map(name -> String.format("%s-%s.cdx", name, project.getVersion())));
            });
        });

        ReportingExtension reporting = project.getExtensions().getByType(ReportingExtension.class);

        cyclonedxBom.configure(cdxBom -> {
            cdxBom.getDestination().convention(reporting.getBaseDirectory().getAsFile().map(baseDir -> new File(baseDir, "sbom")));
        });

        Provider<File> jsonFile = cyclonedxBom.flatMap(CycloneDxTask::getDestination)
                .zip(cyclonedxBom.flatMap(CycloneDxTask::getOutputName), (destination, outputName) -> new File(destination, outputName + ".json"));
        Provider<File> xmlFile = cyclonedxBom.flatMap(CycloneDxTask::getDestination)
                .zip(cyclonedxBom.flatMap(CycloneDxTask::getOutputName), (destination, outputName) -> new File(destination, outputName + ".xml"));

        project.afterEvaluate(p -> {

            if (cyclonedxBom.get().isEnabled()) {
                String outputFormat = cyclonedxBom.get().getOutputFormat().get();
                if ("all".equalsIgnoreCase(outputFormat) || "json".equalsIgnoreCase(outputFormat)) {
                    project.getArtifacts().add(sbomConfiguration.getName(), jsonFile, artifact -> {
                        artifact.setExtension("cdx.json");
                        artifact.setType("cdx");
                        artifact.builtBy(cyclonedxBom);
                    });
                }
                if ("all".equalsIgnoreCase(outputFormat) || "xml".equalsIgnoreCase(outputFormat)) {
                    project.getArtifacts().add(sbomConfiguration.getName(), xmlFile, artifact -> {
                        artifact.setExtension("cdx.xml");
                        artifact.setType("cdx");
                        artifact.builtBy(cyclonedxBom);
                    });
                }
            }

            project.getPlugins().withType(JavaPlugin.class, jp -> {
                Util.getJavaSoftwareComponent(project).addVariantsFromConfiguration(sbomConfiguration, details -> {
                });
            });

        });

    }
}
