package de.materna.cms.gradle.plugins.ci;

import lombok.extern.slf4j.Slf4j;
import org.gradle.api.Plugin;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.Logger;

@Slf4j
public class CiInitPlugin implements Plugin<Gradle> {
    @Override
    public void apply(Gradle gradle) {
        ((Logger) log).lifecycle(
                "Materna CMS CI Defaults ({}) werden für diesen Build ({}) genutzt.",
                getClass().getPackage().getImplementationVersion(),
                gradle.getStartParameter().getCurrentDir()
        );

        gradle.getPlugins().apply(JavaLogsInitPlugin.class);
    }
}
