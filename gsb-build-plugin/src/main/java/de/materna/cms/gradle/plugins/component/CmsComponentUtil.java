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
        Configuration gsbComponent = project.getConfigurations().maybeCreate("cmsComponent");

        ObjectFactory objectFactory = project.getObjects();

        gsbComponent.getAttributes().attribute(Bundling.BUNDLING_ATTRIBUTE, objectFactory.named(Bundling.class, Bundling.EXTERNAL));
        gsbComponent.getAttributes().attribute(Category.CATEGORY_ATTRIBUTE, objectFactory.named(Category.class, "cms-component"));

        return gsbComponent;
    }

    public static Configuration maybeCreateCmsComponentBundleConfiguration(Project project) {
        Configuration gsbComponent = project.getConfigurations().maybeCreate("cmsComponentBundle");

        ObjectFactory objectFactory = project.getObjects();

        gsbComponent.getAttributes().attribute(Bundling.BUNDLING_ATTRIBUTE, objectFactory.named(Bundling.class, Bundling.EMBEDDED));
        gsbComponent.getAttributes().attribute(Category.CATEGORY_ATTRIBUTE, objectFactory.named(Category.class, "cms-component-bundle"));

        return gsbComponent;
    }
}
