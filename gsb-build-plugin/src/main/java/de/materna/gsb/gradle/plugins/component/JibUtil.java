package de.materna.gsb.gradle.plugins.component;

import lombok.experimental.UtilityClass;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;

@UtilityClass
public class JibUtil {


    Provider<String> getBaseImage(Project project) {

        JavaPluginExtension javaPluginExtension = project.getExtensions().findByType(JavaPluginExtension.class);

        if (javaPluginExtension == null) {
            return project.provider(() -> null);
        }

        return javaPluginExtension.getToolchain().getLanguageVersion()
                .map(javaLanguageVersion -> {
                    if (javaLanguageVersion.asInt() <= 8) {
                        return "eclipse-temurin:8-jre";
                    }
                    if (javaLanguageVersion.asInt() <= 11) {
                        return "eclipse-temurin:11-jre";
                    }
                    return "eclipse-temurin:17-jre";
                });

    }
}
