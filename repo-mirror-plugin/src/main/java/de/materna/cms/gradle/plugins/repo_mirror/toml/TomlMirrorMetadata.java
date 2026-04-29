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
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.credentials.Credentials;
import org.tomlj.TomlArray;
import org.tomlj.TomlTable;

public class TomlMirrorMetadata implements MirrorMetadata {

    private String id;
    private TomlArray sourceRepositoryUrls;
    private String mirrorUrl;

    public TomlMirrorMetadata(String key, TomlTable value) {
        this.id = key;
        this.sourceRepositoryUrls = value.getArray("source-repository-urls");
        this.mirrorUrl = value.getString("mirror-url");
    }

    @Override
    public boolean matches(MavenArtifactRepository repository) {
        for (int i = 0; i < sourceRepositoryUrls.size(); i++) {
            String sourceUrl = sourceRepositoryUrls.getString(i);
            if (MirrorMetadata.urlEquals(sourceUrl, repository.getUrl().toString())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getUrl() {
        return mirrorUrl;
    }

    @Override
    public Credentials getCredentials() {
        return null;
    }
}
