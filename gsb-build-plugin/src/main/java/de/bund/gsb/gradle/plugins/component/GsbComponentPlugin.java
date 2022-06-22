package de.bund.gsb.gradle.plugins.component;

import com.google.cloud.tools.jib.gradle.ContainerParameters;
import com.google.cloud.tools.jib.gradle.JibExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.distribution.Distribution;
import org.gradle.api.distribution.DistributionContainer;
import org.gradle.api.distribution.plugins.DistributionPlugin;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.JavaApplication;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.resources.TextResource;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.application.CreateStartScripts;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.War;
import org.gradle.jvm.application.scripts.TemplateBasedScriptGenerator;
import org.springframework.boot.gradle.tasks.bundling.BootWar;

import java.util.List;

public class GsbComponentPlugin implements Plugin<Project> {

    private Project project;
    private Distribution mainDistribution;
    private GsbComponentExtension extension;

    private TaskProvider<CreateStartScripts> createStartScriptsTaskProvider;

    @Override
    public void apply(Project project) {
        this.project = project;
        ObjectFactory objectFactory = project.getObjects();
        extension = project.getExtensions().create("gsbComponent", GsbComponentExtension.class);

        extension.getName().convention(project.getName());

        project.getPlugins().apply(DistributionPlugin.class);

        DistributionContainer distributions = project.getExtensions().getByType(DistributionContainer.class);
        mainDistribution = distributions.getByName("main");

        mainDistribution.getDistributionBaseName().set(extension.getName());

        Configuration configuration = project.getConfigurations().create("gsbComponents");
        configuration.getAttributes().attribute(Bundling.BUNDLING_ATTRIBUTE, objectFactory.named(Bundling.class, Bundling.EXTERNAL));
        configuration.getAttributes().attribute(Category.CATEGORY_ATTRIBUTE, objectFactory.named(Category.class, "gsb-component"));

        project.getArtifacts().add(configuration.getName(), project.getTasks().named("distZip"));

        project.getComponents().all(softwareComponent -> {
            project.getLogger().warn("{} -> {} : {}", softwareComponent.getName(), softwareComponent.getClass(), softwareComponent);

            if(softwareComponent instanceof AdhocComponentWithVariants) {
                ((AdhocComponentWithVariants) softwareComponent).addVariantsFromConfiguration(configuration, action -> {

                });
            }
        });

        project.getPlugins().withType(WarPlugin.class, this::configureWarComponent);
        project.getPlugins().withType(ApplicationPlugin.class, this::configureApplicationComponent);

        project.getPlugins().withId("com.google.cloud.tools.jib", this::configureJib);

        project.afterEvaluate(p -> {
            if (extension.getOverlay().get() && createStartScriptsTaskProvider != null) {
                createStartScriptsTaskProvider.configure(css -> css.setEnabled(false));
            }
        });

    }

    void configureApplicationComponent(ApplicationPlugin applicationPlugin) {
        JavaApplication javaApplication = project.getExtensions().getByType(JavaApplication.class);
        javaApplication.setApplicationName(extension.getName().get());

        createStartScriptsTaskProvider = project.getTasks().named(ApplicationPlugin.TASK_START_SCRIPTS_NAME, CreateStartScripts.class);

        createStartScriptsTaskProvider.configure(startScripts -> {
            startScripts.setClasspath(project.files(project.file("*")));

            TextResource unixStartScript = project.getResources()
                    .getText()
                    .fromUri(getClass().getClassLoader().getResource("de/materna/gsb/gradle/plugins/applicationUnixStartScript.txt"));
            TextResource windowsStartScript = project.getResources()
                    .getText()
                    .fromUri(getClass().getClassLoader()
                            .getResource("de/materna/gsb/gradle/plugins/applicationWindowsStartScript.txt"));

            ((TemplateBasedScriptGenerator) startScripts.getUnixStartScriptGenerator()).setTemplate(unixStartScript);
            ((TemplateBasedScriptGenerator) startScripts.getWindowsStartScriptGenerator()).setTemplate(windowsStartScript);
        });

        project.afterEvaluate(p -> {
            javaApplication.setApplicationName(extension.getName().get());
            if (extension.getOverlay().get()) {
                p.getTasks().named(JavaPlugin.JAR_TASK_NAME, Jar.class, jar -> jar.getArchiveAppendix().set(p.getRootProject().getName()));
            }
        });
    }

    void configureWarComponent(WarPlugin warPlugin) {
        Property<War> warTask = project.getObjects().property(War.class);

        warTask.convention(project.getTasks().named("war", War.class));

        project.getPlugins().withId("org.springframework.boot", sbp -> {
            TaskProvider<BootWar> bootWar = project.getTasks().named("bootWar", BootWar.class);
            warTask.set(bootWar);

            createStartScriptsTaskProvider = project.getTasks().register(ApplicationPlugin.TASK_START_SCRIPTS_NAME, CreateStartScripts.class, startScripts -> {
                startScripts.getMainClass().set(bootWar.flatMap(BootWar::getMainClass));
                startScripts.setOutputDir(project.getLayout().getBuildDirectory().dir("gsbScripts").get().getAsFile());
                startScripts.setApplicationName(extension.getName().get());

                TextResource unixStartScript = project.getResources()
                        .getText()
                        .fromUri(getClass().getClassLoader()
                                .getResource("de/materna/gsb/gradle/plugins/explodedWarUnixStartScript.txt"));
                ((TemplateBasedScriptGenerator) startScripts.getUnixStartScriptGenerator()).setTemplate(unixStartScript);

                TextResource windowsStartScript = project.getResources()
                        .getText()
                        .fromUri(getClass().getClassLoader()
                                .getResource("de/materna/gsb/gradle/plugins/explodedWarWindowsStartScript.txt"));
                ((TemplateBasedScriptGenerator) startScripts.getWindowsStartScriptGenerator()).setTemplate(windowsStartScript);
            });

            mainDistribution.contents(dist -> {
                dist.into("bin", binSpec -> {
                    binSpec.from(createStartScriptsTaskProvider);
                    //noinspection OctalInteger
                    binSpec.setFileMode(0755);
                });
            });

        });

        project.afterEvaluate(p -> {
            p.getTasks().getByName("installDist").dependsOn(warTask);
            p.getTasks().getByName("distZip").dependsOn(warTask);
            p.getTasks().getByName("distTar").dependsOn(warTask);
            mainDistribution.contents(distContent -> {
                distContent.from(p.zipTree(warTask.flatMap(War::getArchiveFile)), spec -> {
                    spec.exclude("org/springframework/boot/loader/**");
                    spec.setIncludeEmptyDirs(false);
                });
            });
        });
    }

    private void configureJib(Plugin<?> plugin) {
        JibExtension jibExtension = project.getExtensions().getByType(JibExtension.class);
        ContainerParameters container = jibExtension.getContainer();

        jibExtension.getFrom().setImage("eclipse-temurin:11");
        container.setCreationTime("USE_CURRENT_TIMESTAMP");

        project.afterEvaluate(p -> {
            JavaApplication javaApplication = project.getExtensions().findByType(JavaApplication.class);

            if (javaApplication != null) {
                if (container.getJvmFlags() == null || container.getJvmFlags().isEmpty()) {
                    container.setJvmFlags((List<String>) javaApplication.getApplicationDefaultJvmArgs());
                }
                if (container.getMainClass() == null) {
                    container.setMainClass(javaApplication.getMainClass().get());
                }
            }

        });
    }
}
