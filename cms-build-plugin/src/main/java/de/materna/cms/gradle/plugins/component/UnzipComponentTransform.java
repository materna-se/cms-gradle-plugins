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

package de.materna.cms.gradle.plugins.component;

import de.materna.cms.gradle.plugins.Util;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.file.ArchiveOperations;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.io.File;

public abstract class UnzipComponentTransform implements TransformAction<TransformParameters.None> {

    @InputArtifact
    protected abstract Provider<FileSystemLocation> getInputArtifact();

    @Inject
    public abstract FileSystemOperations getFileSystemOperations();

    @Inject
    public abstract ArchiveOperations getArchiveOperations();

    @Override
    public void transform(TransformOutputs outputs) {
        File input = getInputArtifact().get().getAsFile();
        File dir = outputs.dir(input.getName() + "-unzipped");

        getFileSystemOperations().sync(s -> {
            s.from(getArchiveOperations().zipTree(input), Util::stripFirstPathSegment);
            s.into(dir);
        });

    }
}
