/*
 * Copyright 2025 Materna Information & Communications SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.BasePluginExtension;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.ReportingBasePlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.reporting.ReportingExtension;
import org.gradle.api.tasks.TaskProvider;

public class SbomPlugin implements Plugin<Project> {

    public static final String SBOM_CONFIGURATION = "sbom";

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(ReportingBasePlugin.class);
        project.getPlugins().apply(CycloneDxPlugin.class);
        project.getPlugins().apply(SbomCachingPlugin.class);

        Configuration sbomConfiguration = project.getConfigurations().maybeCreate(SBOM_CONFIGURATION);
        sbomConfiguration.setCanBeResolved(false);
        sbomConfiguration.setCanBeConsumed(true);

        sbomConfiguration.getAttributes().attribute(Category.CATEGORY_ATTRIBUTE, project.getObjects().named(Category.class, "sbom"));
        sbomConfiguration.getAttributes().attribute(Bundling.BUNDLING_ATTRIBUTE, project.getObjects().named(Bundling.class, Bundling.EMBEDDED));
        sbomConfiguration.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.VERSION_CATALOG));

        TaskProvider<CycloneDxTask> cyclonedxBom = project.getTasks().named("cyclonedxBom", CycloneDxTask.class, cycloneDxTask -> {
            cycloneDxTask.getSchemaVersion().convention("1.6");
        });

        project.getPlugins().apply(BasePlugin.class);
        BasePluginExtension base = project.getExtensions().getByType(BasePluginExtension.class);

        ReportingExtension reporting = project.getExtensions().getByType(ReportingExtension.class);

        cyclonedxBom.configure(cdxBom -> {

            Provider<Directory> baseDir = reporting.getBaseDirectory().dir("sbom");

            cdxBom.getDestination().set(baseDir.map(Directory::getAsFile));

            cdxBom.getJsonOutput().set(baseDir.flatMap(d -> d.file(base.getArchivesName().map(name -> String.format("%s-%s.cdx.json", name, project.getVersion())))));
            cdxBom.getXmlOutput().set(baseDir.flatMap(d -> d.file(base.getArchivesName().map(name -> String.format("%s-%s.cdx.xml", name, project.getVersion())))));

        });

        project.afterEvaluate(p -> {

            if (cyclonedxBom.get().isEnabled()) {
                project.getArtifacts().add(sbomConfiguration.getName(), cyclonedxBom.get().getJsonOutput(), artifact -> {
                    artifact.setExtension("cdx.json");
                    artifact.setType("cdx");
                    artifact.builtBy(cyclonedxBom);
                });

                project.getArtifacts().add(sbomConfiguration.getName(), cyclonedxBom.get().getXmlOutput(), artifact -> {
                    artifact.setExtension("cdx.xml");
                    artifact.setType("cdx");
                    artifact.builtBy(cyclonedxBom);
                });
            }

            project.getPlugins().withType(JavaPlugin.class, jp -> {
                Util.getJavaSoftwareComponent(project).addVariantsFromConfiguration(sbomConfiguration, details -> {
                });
            });

        });

    }
}
