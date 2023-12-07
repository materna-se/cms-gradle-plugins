package de.materna.cms.gradle.plugins.jib;

import lombok.experimental.UtilityClass;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

@UtilityClass
public class JibUtil {


    public Provider<String> getBaseImage(Project project) {

        JavaPluginExtension javaPluginExtension = project.getExtensions().findByType(JavaPluginExtension.class);

        if (javaPluginExtension == null) {
            return project.provider(() -> null);
        }

        return javaPluginExtension.getToolchain()
                .getLanguageVersion()
                .map(JibUtil::getBaseImage);

    }

    public String getBaseImage(JavaLanguageVersion javaLanguageVersion) {
        if (javaLanguageVersion.asInt() <= 8) {
            return "eclipse-temurin:8-jre";
        }
        if (javaLanguageVersion.asInt() <= 11) {
            return "eclipse-temurin:11-jre";
        }
        if (javaLanguageVersion.asInt() <= 17) {
            return "eclipse-temurin:17-jre";
        }
        return "eclipse-temurin:21-jre";
    }
}
