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

import org.cyclonedx.gradle.BaseCyclonedxTask;
import org.cyclonedx.model.OrganizationalEntity;
import org.cyclonedx.model.organization.PostalAddress;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static de.materna.cms.gradle.plugins.materna_metadata.MaternaMetadataPlugin.MATERNA_NAME;
import static de.materna.cms.gradle.plugins.materna_metadata.MaternaMetadataPlugin.MATERNA_URL;

public class CycloneDxMetadataPlugin implements Plugin<Project> {

    static OrganizationalEntity materna = getMaterna();

    private static OrganizationalEntity getMaterna() {
        OrganizationalEntity materna = new OrganizationalEntity();
        materna.setName(MATERNA_NAME);
        materna.setUrls(List.of(MATERNA_URL));
        materna.setAddress(getPostalAddress());
        return materna;
    }

    private static @NotNull PostalAddress getPostalAddress() {
        PostalAddress rss = new PostalAddress();
        rss.setStreetAddress("Robert-Schuman-Straße 20");
        rss.setPostalCode("44263");
        rss.setLocality("Dortmund");
        rss.setCountry("DE");
        return rss;
    }

    @Override
    public void apply(Project project) {
        project.getTasks().withType(BaseCyclonedxTask.class)
                .configureEach(cyclonedxBom -> cyclonedxBom.getOrganizationalEntity().convention(materna));
    }
}
