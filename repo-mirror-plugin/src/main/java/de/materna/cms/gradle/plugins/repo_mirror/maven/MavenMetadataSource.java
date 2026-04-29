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

package de.materna.cms.gradle.plugins.repo_mirror.maven;

import de.materna.cms.gradle.plugins.repo_mirror.MirrorMetadata;
import de.materna.cms.gradle.plugins.repo_mirror.MirrorMetadataSource;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.DefaultSettingsReader;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MavenMetadataSource implements MirrorMetadataSource {

    Settings mavenSettings;

    public MavenMetadataSource(Settings mavenSettings) {
        this.mavenSettings = mavenSettings;
    }

    public MavenMetadataSource(File settingsXml) throws IOException {

        mavenSettings = new DefaultSettingsReader().read(settingsXml, Collections.emptyMap());

    }

    public List<MirrorMetadata> getMetadata()  {

        return mavenSettings.getMirrors().stream()
                .map(this::toMetadata)
                .collect(Collectors.toList());

    }

    private MirrorMetadata toMetadata(Mirror mavenMirror) {
        MavenMirrorMetadata mavenMirrorMetadata = new MavenMirrorMetadata(mavenMirror);
        mavenMirrorMetadata.setServer(mavenSettings.getServer(mavenMirror.getId()));


        return mavenMirrorMetadata;
    }
}
