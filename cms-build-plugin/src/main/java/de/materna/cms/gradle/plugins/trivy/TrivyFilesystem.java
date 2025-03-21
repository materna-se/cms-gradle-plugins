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
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

@CacheableTask
public abstract class TrivyFilesystem extends AbstractTrivyTask {

    @InputDirectory
    public abstract DirectoryProperty getSourceDirectory();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @Input
    @Optional
    public abstract Property<Boolean> getOfflineScan();

    @Input
    @Optional
    public abstract ListProperty<String> getScanners();

    @Input
    @Optional
    public abstract Property<String> getFormat();

    public TrivyFilesystem() {
        super();
        args("filesystem");
    }

    @Override
    protected void exec() {

        if (getOfflineScan().getOrElse(false)) {
            args("--offline-scan");
        }

        if (getScanners().isPresent()) {
            args("--scanners", String.join(",", getScanners().get()));
        }

        if (getFormat().isPresent()) {
            args("--format", getFormat().get());
        }

        if (getOutputFile().isPresent()) {
            args("--output", getOutputFile().get().getAsFile().getAbsolutePath());
        }

        args(getSourceDirectory().get().getAsFile().getAbsolutePath());

        super.exec();
    }
}