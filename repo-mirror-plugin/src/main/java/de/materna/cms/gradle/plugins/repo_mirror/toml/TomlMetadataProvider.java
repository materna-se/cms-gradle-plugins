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

package de.materna.cms.gradle.plugins.repo_mirror.toml;

import de.materna.cms.gradle.plugins.repo_mirror.MirrorMetadata;
import de.materna.cms.gradle.plugins.repo_mirror.MirrorMetadataProvider;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.invocation.Gradle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TomlMetadataProvider implements MirrorMetadataProvider {
    @Override
    public List<MirrorMetadata> getMirrorMetadata(Gradle gradle) {

        List<MirrorMetadata> providers = new ArrayList<>(2);

        File gradleUserHomeMirrros = new File(gradle.getGradleUserHomeDir(), "mirrors.toml");
        File gradleHomeMirrros = new File(gradle.getGradleHomeDir(), "mirrors.toml");

        if (gradleUserHomeMirrros.isFile()) {
            try {
                providers.addAll(new TomlMetadataSource(gradleUserHomeMirrros).getMetadata());
            } catch (IOException e) {
                log.error("Failed to load Toml Metadata Source", e);
            }
        }

        if (gradleHomeMirrros.isFile()) {
            try {
                providers.addAll(new TomlMetadataSource(gradleHomeMirrros).getMetadata());
            } catch (IOException e) {
                log.error("Failed to load Toml Metadata Source", e);
            }
        }

        return providers;
    }
}
