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

package de.materna.cms.gradle.plugins.mirrors.toml;

import de.materna.cms.gradle.plugins.mirrors.MavenRepositoryMirror;
import de.materna.cms.gradle.plugins.mirrors.MavenRepositoryMirrorProvider;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.invocation.Gradle;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class TomlMavenRepositoryMirrorProvider implements MavenRepositoryMirrorProvider {

    @Override
    public List<MavenRepositoryMirror> getMirrorMetadata(Gradle gradle) {

        List<MavenRepositoryMirror> providers = new ArrayList<>(2);

        File gradleUserHomeMirrros = new File(gradle.getGradleUserHomeDir(), "mirrors.toml");
        File gradleHomeMirrros = new File(gradle.getGradleHomeDir(), "mirrors.toml");

        if (gradleUserHomeMirrros.isFile()) {
            try {
                providers.addAll(getMirrors(gradleUserHomeMirrros));
            } catch (IOException e) {
                log.error("Failed to load Toml Metadata Source", e);
            }
        }

        if (gradleHomeMirrros.isFile()) {
            try {
                providers.addAll(getMirrors(gradleHomeMirrros));
            } catch (IOException e) {
                log.error("Failed to load Toml Metadata Source", e);
            }
        }

        return providers;
    }

    public static List<MavenRepositoryMirror> getMirrors(File tomlFile) throws IOException {
        TomlParseResult parse = Toml.parse(tomlFile.toPath());


        return parse.entrySet().stream()
                .map(entry -> new TomlMavenRepositoryMirror(entry.getKey(), (TomlTable) entry.getValue()))
                .collect(Collectors.toList());
    }
}
