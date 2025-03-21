/*
 * Copyright 2025 Materna Information & Communications SE
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

package de.materna.cms.gradle.plugins.trivy;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

public abstract class AbstractTrivyTask extends Exec {

    /**
     *  cache directory.
     */
    @Internal
    public abstract DirectoryProperty getCacheDir();

    /**
     * config path (default "trivy.yaml")
     */
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getConfig();

    /**
     * allow insecure server connections
     */
    @Input
    @Optional
    public abstract Property<Boolean> getInsecure();

    /**
     * suppress progress bar and log output.
     */
    @Console
    public abstract Property<Boolean> getQuiet();

    public AbstractTrivyTask() {
        setExecutable("trivy");
    }

    @Override
    protected void exec() {

        if (getCacheDir().isPresent()) {
            args("--cache-dir", getCacheDir().get().getAsFile().getAbsolutePath());
        }

        if (getConfig().isPresent()) {
            args("--config", getConfig().get().getAsFile().getAbsolutePath());
        }

        if (getInsecure().getOrElse(false)) {
            args("--insecure");
        }

        if (getQuiet().getOrElse(false)) {
            args("--quiet");
        }

        super.exec();
    }
}
