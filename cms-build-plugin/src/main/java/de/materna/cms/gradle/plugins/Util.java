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

package de.materna.cms.gradle.plugins;

import lombok.experimental.UtilityClass;
import org.gradle.api.Project;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.file.CopySpec;

@UtilityClass
public class Util {

    public static void stripFirstPathSegment(CopySpec copySpec) {
        copySpec.eachFile(fileCopyDetails -> {
            String oldSourcePath = fileCopyDetails.getSourcePath();
            String newSourcePath = oldSourcePath.substring(oldSourcePath.indexOf("/"));

            String path = fileCopyDetails.getPath();
            fileCopyDetails.setPath(path.replace(oldSourcePath, newSourcePath));
        });
        copySpec.setIncludeEmptyDirs(false);
    }

    public static AdhocComponentWithVariants getJavaSoftwareComponent(Project project) {
        SoftwareComponent java = project.getComponents().getByName("java");
        return (AdhocComponentWithVariants) java;
    }
}
