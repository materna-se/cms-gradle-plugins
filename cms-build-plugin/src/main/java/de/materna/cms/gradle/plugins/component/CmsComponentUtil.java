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

package de.materna.cms.gradle.plugins.component;

import lombok.experimental.UtilityClass;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.model.ObjectFactory;

@UtilityClass
public class CmsComponentUtil {

    public static Configuration maybeCreateCmsComponentConfiguration(Project project) {
        Configuration cmsComponent = project.getConfigurations().maybeCreate("cmsComponent");

        ObjectFactory objectFactory = project.getObjects();

        cmsComponent.getAttributes().attribute(Bundling.BUNDLING_ATTRIBUTE, objectFactory.named(Bundling.class, Bundling.EXTERNAL));
        cmsComponent.getAttributes().attribute(Category.CATEGORY_ATTRIBUTE, objectFactory.named(Category.class, "cms-component"));

        return cmsComponent;
    }

    public static Configuration maybeCreateCmsComponentBundleConfiguration(Project project) {
        Configuration cmsComponentBundle = project.getConfigurations().maybeCreate("cmsComponentBundle");

        ObjectFactory objectFactory = project.getObjects();

        cmsComponentBundle.getAttributes().attribute(Bundling.BUNDLING_ATTRIBUTE, objectFactory.named(Bundling.class, Bundling.EMBEDDED));
        cmsComponentBundle.getAttributes().attribute(Category.CATEGORY_ATTRIBUTE, objectFactory.named(Category.class, "cms-component-bundle"));

        return cmsComponentBundle;
    }
}
