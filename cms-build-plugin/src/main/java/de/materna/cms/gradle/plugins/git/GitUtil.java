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

package de.materna.cms.gradle.plugins.git;

import lombok.experimental.UtilityClass;

import java.io.IOException;

import static org.codehaus.groovy.runtime.ProcessGroovyMethods.execute;
import static org.codehaus.groovy.runtime.ProcessGroovyMethods.getText;

@UtilityClass
public class GitUtil {


    public static boolean isGitDirty() throws IOException {
        return getText(execute("git describe --dirty --always")).trim().endsWith("-dirty");
    }

    public static boolean isGitConflict() throws IOException {
        return !getText(execute("git ls-files -u")).trim().isEmpty();
    }

    public static String getCurrentGitHash() throws IOException {
        return getText(execute("git rev-parse -q HEAD")).trim();
    }
}
