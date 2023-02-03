package de.materna.gsb.gradle.plugins.component;

import lombok.experimental.UtilityClass;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.model.ObjectFactory;

@UtilityClass
public class GsbComponentUtil {

    public static Configuration maybeCreateGsbComponentConfiguration(Project project) {
        Configuration gsbComponent = project.getConfigurations().maybeCreate("gsbComponent");

        ObjectFactory objectFactory = project.getObjects();

        gsbComponent.getAttributes().attribute(Bundling.BUNDLING_ATTRIBUTE, objectFactory.named(Bundling.class, Bundling.EXTERNAL));
        gsbComponent.getAttributes().attribute(Category.CATEGORY_ATTRIBUTE, objectFactory.named(Category.class, "gsb-component"));

        return gsbComponent;
    }

    public static Configuration maybeCreateGsbComponentBundleConfiguration(Project project) {
        Configuration gsbComponent = project.getConfigurations().maybeCreate("gsbComponentBundle");

        ObjectFactory objectFactory = project.getObjects();

        gsbComponent.getAttributes().attribute(Bundling.BUNDLING_ATTRIBUTE, objectFactory.named(Bundling.class, Bundling.EMBEDDED));
        gsbComponent.getAttributes().attribute(Category.CATEGORY_ATTRIBUTE, objectFactory.named(Category.class, "gsb-component-bundle"));

        return gsbComponent;
    }
}
