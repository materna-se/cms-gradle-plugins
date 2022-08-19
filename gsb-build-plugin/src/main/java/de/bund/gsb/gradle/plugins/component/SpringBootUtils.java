package de.bund.gsb.gradle.plugins.component;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.file.CopySpec;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * @see https://github.com/spring-projects/spring-boot/issues/22036
 */
@Slf4j
@UtilityClass
public class SpringBootUtils {

    public boolean isDependenciesStarter(File file) {

        if (!file.isFile()) {
            return false;
        }

        if (!file.getName().endsWith(".jar")) {
            return false;
        }

        try (JarFile jarFile = new JarFile(file, false)) {
            Attributes mainAttributes = jarFile.getManifest().getMainAttributes();

            Object jarType = mainAttributes.getValue("Spring-Boot-Jar-Type");

            return "dependencies-starter".equals(jarType);
        } catch (Exception e) {
            log.warn("Fehler beim Lesen der Datei {}", file, e);
            return false;
        }

    }

    /**
     * @see org.springframework.boot.gradle.plugin.JarTypeFileSpec
     */
    public static void excludeDependenciesStarters(CopySpec copySpec) {
        copySpec.eachFile(fileCopyDetails -> {
            if (fileCopyDetails.getPath().contains("lib") && isDependenciesStarter(fileCopyDetails.getFile())) {
                log.info("Exclude {}", fileCopyDetails.getPath());
                fileCopyDetails.exclude();
            }
        });
    }
}
