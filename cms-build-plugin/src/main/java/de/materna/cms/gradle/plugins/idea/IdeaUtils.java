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

package de.materna.cms.gradle.plugins.idea;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

import java.io.File;

public class IdeaUtils {

    public static boolean isIntellJSync(Project project) {

        Provider<String> ideaSyncActive = project.getProviders().systemProperty("idea.sync.active");

        if (ideaSyncActive.isPresent() && "true".equalsIgnoreCase(ideaSyncActive.get())) {
            return true;
        }

        for (File initScript : project.getGradle().getStartParameter().getAllInitScripts()) {
            if (initScript.getName().startsWith("sync.studio.tooling")) {
                return true;
            }

            if (initScript.getName().equals("ijInit1.gradle")) {
                return true;
            }
        }

        return false;
    }
}
