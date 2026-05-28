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
import lombok.Data;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Server;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.credentials.Credentials;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class MavenSettingsMirror implements MavenRepositoryMirror {

    private final Mirror mirror;
    private Server server;

    private List<String> mirrorOf;

    public MavenSettingsMirror(Mirror mavenMirror) {
        this.mirror = mavenMirror;
        mirrorOf = Arrays.stream(mavenMirror.getMirrorOf().split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    @Override
    public boolean matches(MavenArtifactRepository repository) {

        for (String mirrorOf : mirror.getMirrorOf().split(",")) {
            if (mirrorOf == null) {
                continue;
            }
            mirrorOf = mirrorOf.trim();

            if (mirrorOf.isEmpty()) {
                continue;
            }

            if (mirrorOf.startsWith("!")) {
                if (!matches(repository, mirrorOf.substring(1))) {
                    return true;
                }
            } else {
                if (matches(repository, mirrorOf)) {
                    return true;
                }
            }

        }


        return false;
    }

    @Override
    public String getUrl() {
        return mirror.getUrl();
    }

    @Override
    public Credentials getCredentials() {
        if (server == null) {
            return null;
        }

        if (server.getUsername() != null && server.getPassword() != null) {
            return new MavenServerPasswordCredentials(server);
        }

        return null;
    }

    private boolean matches(MavenArtifactRepository repository, String mirrorOf) {
        if (mirrorOf == null || mirrorOf.isEmpty()) {
            return false;
        }

        if (mirrorOf.equals("*"))
            return true;

        if (mirrorOf.equals("central")) {
            if (repository.getName().equals(RepositoryHandler.DEFAULT_MAVEN_CENTRAL_REPO_NAME)) {
                return true;
            }
            if (repository.getUrl().toString().equals(RepositoryHandler.MAVEN_CENTRAL_URL)) {
                return true;
            }
        }

        if (mirrorOf.equals(repository.getName()) || MavenRepositoryMirror.urlEquals(mirrorOf, repository.getUrl().toString())) {
            return true;
        }

        return false;
    }
}
