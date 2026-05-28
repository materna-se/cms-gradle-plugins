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
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MavenSettingsMirrorProviderTest {


    MavenSettingsMirrorProvider provider;
    Project project;

    @BeforeEach
    void setUp() {
        provider = new MavenSettingsMirrorProvider();
        project = ProjectBuilder.builder().build();

    }

    @Test
    void getSources() {
        List<MavenRepositoryMirror> providers = provider.getMirrorMetadata(project.getGradle());

        assertThat(providers).isNotNull();
    }

}