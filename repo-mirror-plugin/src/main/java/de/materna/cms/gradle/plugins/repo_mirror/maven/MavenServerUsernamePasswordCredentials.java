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

import lombok.Data;
import org.apache.maven.settings.Server;
import org.gradle.api.artifacts.repositories.PasswordCredentials;
import org.jspecify.annotations.Nullable;

@Data
public class MavenServerUsernamePasswordCredentials implements PasswordCredentials {

    private final Server server;

    @Override
    public @Nullable String getUsername() {
        return server.getUsername();
    }

    @Override
    public void setUsername(@Nullable String userName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable String getPassword() {
        return server.getPassword();
    }

    @Override
    public void setPassword(@Nullable String password) {
        throw new UnsupportedOperationException();
    }
}
