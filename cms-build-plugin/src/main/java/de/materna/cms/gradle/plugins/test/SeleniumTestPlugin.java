package de.materna.cms.gradle.plugins.test;


import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;

import java.io.File;

/**
 * Gradle-Plugin für Selenium-basierte Integrations-Tests.
 *
 * <p>Dieses Plugin basiert auf dem {@link IntegrationTestPlugin} und fügt nur Selenium-Spezifische Dependencies hinzu.
 *
 * @see IntegrationTestPlugin
 */
public class SeleniumTestPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(IntegrationTestPlugin.class);

        project.getDependencies().add("integrationTestImplementation", "org.seleniumhq.selenium:selenium-java:4.13.0");
        project.getDependencies().add("integrationTestImplementation", "org.assertj:assertj-core");
        project.getDependencies().add("integrationTestImplementation", "de.materna.cms.tools:cms-selenium-test-utils:"
                + SeleniumTestPlugin.class.getPackage().getImplementationVersion()
        );

        Provider<Directory> geckoDir = project.getLayout().getBuildDirectory().dir("selenium/drivers/gecko");
        TaskProvider<PrepareGeckoDriver> prepareGeckoDriverTaskProvider = project.getTasks()
                .register("prepareGeckoDriver",
                        PrepareGeckoDriver.class,
                        prepareGeckoDriver -> {
                            prepareGeckoDriver.getOutputDir().set(geckoDir);
                            prepareGeckoDriver.getVersion().set("0.24.0");
                        });

        project.getTasks().named("integrationTest", Test.class, integrationTest -> {
            integrationTest.dependsOn(prepareGeckoDriverTaskProvider);
            integrationTest.systemProperty("webdriver.gecko.driver", new File(geckoDir.get().getAsFile(), "geckodriver"));
        });
    }
}