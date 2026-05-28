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

package de.materna.cms.gradle.plugins.mirrors;

import org.gradle.api.Plugin;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.credentials.Credentials;
import org.gradle.api.credentials.PasswordCredentials;
import org.gradle.api.invocation.Gradle;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author Lars Grefer
 * @see <a href="https://blog.gradle.org/maven-central-mirror">https://blog.gradle.org/maven-central-mirror</a>
 */
public class MavenRepositoryMirrorsPlugin implements Plugin<Gradle> {

    private final List<MavenRepositoryMirror> mirrors = new ArrayList<>();

    @Override
    public void apply(@NonNull Gradle gradle) {

        ServiceLoader.load(MavenRepositoryMirrorProvider.class).forEach(provider -> {
            mirrors.addAll(provider.getMirrorMetadata(gradle));
        });

        if (GradleVersionUtil.isAtLeastVersion("6.0")) {
            gradle.beforeSettings(settings -> {
                applyMirrors(settings.getBuildscript().getRepositories());
                applyMirrors(settings.getPluginManagement().getRepositories());
            });
        }

        if (GradleVersionUtil.isAtLeastVersion("6.8")) {
            gradle.settingsEvaluated(settings -> {
                applyMirrors(settings.getDependencyResolutionManagement().getRepositories());
            });
        }

        if (GradleVersionUtil.isAtLeastVersion("3.4")) {
            gradle.beforeProject(project -> {
                applyMirrors(project.getBuildscript().getRepositories());
            });

            gradle.afterProject(project -> {
                applyMirrors(project.getRepositories());
            });
        }

    }

    void applyMirrors(RepositoryHandler repositoryHandler) {

        repositoryHandler.withType(MavenArtifactRepository.class)
                .configureEach(this::applyMirror);

    }

    private void applyMirror(MavenArtifactRepository repository) {

        for (MavenRepositoryMirror mirror : mirrors) {
            if (mirror.matches(repository)) {

                repository.setUrl(mirror.getUrl());

                Credentials credentials = mirror.getCredentials();

                if (credentials instanceof PasswordCredentials) {
                    PasswordCredentials mirrorCredentials = (PasswordCredentials) credentials;
                    repository.credentials(repoCredentials -> {
                        repoCredentials.setUsername(mirrorCredentials.getUsername());
                        repoCredentials.setPassword(mirrorCredentials.getPassword());
                    });
                }

            }
        }

    }
}
