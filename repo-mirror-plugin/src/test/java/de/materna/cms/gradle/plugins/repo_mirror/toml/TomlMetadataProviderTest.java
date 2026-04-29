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
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class TomlMetadataProviderTest {

    @Test
    void test() throws IOException {

        TomlMetadataSource tomlMetadataProvider = new TomlMetadataSource(new File("src/test/resources/mirrors.toml"));

        assertThat(tomlMetadataProvider.getMetadata()).hasSize(1);

        MirrorMetadata mirrorMetadata = tomlMetadataProvider.getMetadata().get(0);

        assertThat(mirrorMetadata.getUrl()).isEqualTo("https://artifactory.my-corp.com/libs-release");

    }

}