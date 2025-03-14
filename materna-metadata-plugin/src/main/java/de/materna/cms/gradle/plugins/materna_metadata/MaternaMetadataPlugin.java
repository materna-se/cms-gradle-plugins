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

package de.materna.cms.gradle.plugins.materna_metadata;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.jvm.tasks.Jar;

import java.util.Collections;

public class MaternaMetadataPlugin implements Plugin<Project> {


    @Override
    public void apply(Project project) {
        project.allprojects(this::configureSubproject);
    }

    private void configureSubproject(Project project) {

        project.getTasks().withType(Jar.class).configureEach(jar -> {
            jar.getManifest().attributes(Collections.singletonMap("Implementation-Vendor", "Materna Information & Commundations SE"));
            jar.getManifest().attributes(Collections.singletonMap("Implementation-URL", "https://materna.de"));
            jar.getManifest().attributes(Collections.singletonMap("Implementation-Vendor-Id", "de.materna"));
            jar.getManifest().attributes(Collections.singletonMap("Implementation-Version", project.getVersion()));
        });

        project.getPlugins().withType(MavenPublishPlugin.class, mpp -> {
            project.getExtensions().getByType(PublishingExtension.class).getPublications().withType(MavenPublication.class).configureEach(mavenPublication -> {
                mavenPublication.getPom().organization(org -> {
                    org.getName().convention("Materna Information & Commundations SE");
                    org.getUrl().convention("https://materna.de");
                });
            });
        });

        project.getPlugins().withId("com.google.cloud.tools.jib", plugin -> project.getPlugins().apply(JibMetadataPlugin.class));

        project.getPlugins().withId("org.cyclonedx.bom", plugin -> project.getPlugins().apply(CycloneDxMetadataPlugin.class));

        project.getPlugins().withId("org.jreleaser", plugin -> project.getPlugins().apply(JReleaserMetadataPlugin.class));

    }

}
