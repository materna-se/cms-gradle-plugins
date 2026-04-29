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
import de.materna.cms.gradle.plugins.repo_mirror.MirrorMetadataSource;
import org.gradle.api.invocation.Gradle;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class TomlMetadataSource implements MirrorMetadataSource {

    TomlParseResult tomlParseResult;

    public TomlMetadataSource(File file) throws IOException {
        this.tomlParseResult = Toml.parse(file.toPath());

    }

    public static TomlMetadataSource forGradleHome(Gradle gradle) throws IOException {
        File file = new File(gradle.getGradleUserHomeDir(), "mirrors.toml");
        if (file.exists()) {
            return new TomlMetadataSource(file);
        }

        return null;
    }

    @Override
    public List<MirrorMetadata> getMetadata() {

        return tomlParseResult.entrySet().stream()
                .map(entry -> new TomlMirrorMetadata(entry.getKey(), (TomlTable) entry.getValue()))
                .collect(Collectors.toList());

    }

}
