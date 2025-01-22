package de.materna.cms.gradle.plugins.sbom;

import org.cyclonedx.gradle.CycloneDxTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.ClasspathNormalizer;
import org.gradle.api.tasks.PathSensitivity;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

public class SbomCachingPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.afterEvaluate(p -> {
            project.getTasks().withType(CycloneDxTask.class).configureEach(task -> {

                task.getOutputs().cacheIf(s -> task.getIncludeConfigs().isPresent() && !task.getIncludeConfigs().get().isEmpty());

                //Cache maximal für eine Woche gültig.
                LocalDate now = LocalDate.now();
                task.getInputs().property("_caching-fix_year", now.getYear());
                task.getInputs().property("_caching-fix_week-of-year", now.get(ChronoField.ALIGNED_WEEK_OF_YEAR));

                task.getInputs().property("_caching-fix_version", project.getVersion().toString());

                ConfigurableFileCollection includeConfigs = p.files();
                for (String includeConfig : task.getIncludeConfigs().get()) {
                    Configuration byName = project.getConfigurations().findByName(includeConfig);
                    includeConfigs.from(byName);
                }
                task.getInputs().files(includeConfigs)
                        .withPathSensitivity(PathSensitivity.RELATIVE)
                        .withNormalizer(ClasspathNormalizer.class)
                        .withPropertyName("_caching-fix_includeConfigs");

            });
        });
    }
}
