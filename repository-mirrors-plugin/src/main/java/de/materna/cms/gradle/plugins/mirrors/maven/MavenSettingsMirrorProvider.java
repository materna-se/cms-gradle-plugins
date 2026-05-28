/*
 * Copyright 2026 Materna Information & Communications SE
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

package de.materna.cms.gradle.plugins.mirrors.maven;

import de.materna.cms.gradle.plugins.mirrors.MavenRepositoryMirror;
import de.materna.cms.gradle.plugins.mirrors.MavenRepositoryMirrorProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.*;
import org.gradle.api.invocation.Gradle;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class MavenSettingsMirrorProvider implements MavenRepositoryMirrorProvider {

    @Override
    public List<MavenRepositoryMirror> getMirrorMetadata(Gradle gradle) {

        DefaultSettingsBuilder defaultSettingsBuilder = new DefaultSettingsBuilderFactory().newInstance();

        Settings effectiveSettings;
        try {
            SettingsBuildingResult build = defaultSettingsBuilder.build(getRequest());
            effectiveSettings = build.getEffectiveSettings();
        } catch (SettingsBuildingException e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }

        return getMirrors(effectiveSettings);
    }

    private static List<MavenRepositoryMirror> getMirrors(Settings effectiveSettings) {


        return effectiveSettings.getMirrors().stream()
                .map(mm -> {
                    MavenSettingsMirror mavenMirrorMetadata = new MavenSettingsMirror(mm);
                    mavenMirrorMetadata.setServer(effectiveSettings.getServer(mm.getId()));


                    return mavenMirrorMetadata;
                })
                .collect(Collectors.toList());
    }

    private static @NonNull DefaultSettingsBuildingRequest getRequest() {
        DefaultSettingsBuildingRequest defaultSettingsBuildingRequest = new DefaultSettingsBuildingRequest();

        String userHome = System.getProperty("user.home");
        defaultSettingsBuildingRequest.setUserSettingsFile(new File(userHome + "/.m2/settings.xml"));

        defaultSettingsBuildingRequest.setSystemProperties(System.getProperties());

        return defaultSettingsBuildingRequest;
    }

}
