package de.bund.gsb.gradle.plugins.component;

import com.google.cloud.tools.jib.gradle.ContainerParameters;
import com.google.cloud.tools.jib.gradle.JibExtension;
import de.bund.gsb.gradle.plugins.Util;
import org.codehaus.groovy.runtime.ProcessGroovyMethods;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.distribution.Distribution;
import org.gradle.api.distribution.DistributionContainer;
import org.gradle.api.distribution.plugins.DistributionPlugin;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.internal.file.copy.CopySpecInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.*;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.application.CreateStartScripts;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Tar;
import org.gradle.api.tasks.bundling.War;
import org.gradle.api.tasks.bundling.Zip;
import org.springframework.boot.gradle.plugin.SpringBootPlugin;
import org.springframework.boot.gradle.tasks.bundling.BootWar;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Manifest;

public class GsbComponentPlugin implements Plugin<Project> {

    private Project project;
    private Distribution mainDistribution;
    private GsbComponentExtension extension;

    private TaskProvider<CreateStartScripts> createStartScriptsTaskProvider;
    private Configuration gsbComponent;
    private AdhocComponentWithVariants javaComponent;

    private TaskProvider<Zip> distZip;
    private TaskProvider<Tar> distTar;
    private TaskProvider<Sync> extractComponents;

    private TaskProvider<Sync> installFullDist;
    private TaskProvider<Exec> execFull;
    private TaskProvider<JavaExec> javaExecFull;

    @Override
    public void apply(Project project) {
        this.project = project;
        project.getPlugins().apply(BasePlugin.class);
        ObjectFactory objectFactory = project.getObjects();
        extension = project.getExtensions().create("gsbComponent", GsbComponentExtension.class);

        BasePluginExtension basePluginExtension = project.getExtensions().getByType(BasePluginExtension.class);

        extension.getName().convention(basePluginExtension.getArchivesName());

        project.getPlugins().apply(DistributionPlugin.class);

        DistributionContainer distributions = project.getExtensions().getByType(DistributionContainer.class);
        mainDistribution = distributions.getByName("main");

        mainDistribution.getDistributionBaseName().set(extension.getName());

        distZip = project.getTasks().named("distZip", Zip.class);
        distTar = project.getTasks().named("distTar", Tar.class);

        distZip.configure(task -> task.getArchiveVersion().set(""));
        distTar.configure(task -> task.getArchiveVersion().set(""));

        gsbComponent = project.getConfigurations().create("gsbComponent");
        gsbComponent.getAttributes().attribute(Bundling.BUNDLING_ATTRIBUTE, objectFactory.named(Bundling.class, Bundling.EXTERNAL));
        gsbComponent.getAttributes().attribute(Category.CATEGORY_ATTRIBUTE, objectFactory.named(Category.class, "gsb-component"));

        project.getArtifacts().add(gsbComponent.getName(), distZip);

        extractComponents = project.getTasks().register("extractComponents", Sync.class, task -> {
            task.setGroup("gsb");
            task.into(project.getLayout().getBuildDirectory().dir("gsb/components"));

            for (File gsbComponent : gsbComponent) {
                task.from(project.zipTree(gsbComponent), Util::stripFirstPathSegment);
            }
        });

        Distribution fullDistribution = distributions.create("full");

        fullDistribution.getContents().from(extractComponents);
        fullDistribution.getContents().setDuplicatesStrategy(DuplicatesStrategy.WARN);
        fullDistribution.getContents().with(mainDistribution.getContents());

        installFullDist = project.getTasks().named("installFullDist", Sync.class);

        execFull = project.getTasks().register("gsbRunFull", Exec.class, exec -> {
            exec.setGroup("gsb");
            exec.dependsOn(installFullDist);

            File binDir = new File(installFullDist.get().getDestinationDir(), "bin");
            exec.setExecutable(new File(binDir, extension.getName().get()));
        });
        javaExecFull = project.getTasks().register("gsbRunFullJava", JavaExec.class, exec -> {
            exec.setGroup("gsb");
            exec.dependsOn(installFullDist);
        });

        project.getPlugins().withType(JavaPlugin.class, jp -> {
            javaComponent = (AdhocComponentWithVariants) project.getComponents().getByName("java");
            javaComponent.addVariantsFromConfiguration(gsbComponent, details -> {
            });
        });

        project.getPlugins().withType(WarPlugin.class, this::configureWarComponent);
        project.getPlugins().withType(ApplicationPlugin.class, this::configureApplicationComponent);
        project.getPlugins().withId("org.springframework.boot", this::configureSpringBoot);

        project.getPlugins().withId("com.google.cloud.tools.jib", this::configureJib);

        project.afterEvaluate(p -> {
            if (extension.getOverlay().get() && createStartScriptsTaskProvider != null) {
                createStartScriptsTaskProvider.configure(css -> css.setEnabled(false));
            }
        });

    }

    private void configureSpringBoot(Plugin<Project> plugin) {
        project.getTasks().named(JavaPlugin.JAR_TASK_NAME, Jar.class, jar -> jar.getArchiveClassifier().set(""));
        project.getTasks().named(SpringBootPlugin.BOOT_JAR_TASK_NAME, Jar.class, jar -> jar.getArchiveClassifier().set("boot"));
    }

    void configureApplicationComponent(ApplicationPlugin applicationPlugin) {
        JavaApplication javaApplication = project.getExtensions().getByType(JavaApplication.class);
        javaApplication.setApplicationName(extension.getName().get());

        createStartScriptsTaskProvider = project.getTasks().named(ApplicationPlugin.TASK_START_SCRIPTS_NAME, CreateStartScripts.class);

        createStartScriptsTaskProvider.configure(startScripts -> {
            startScripts.setUnixStartScriptGenerator(new GsbApplicationStartScriptGenerator());
            StartScriptUtil.disableWindowsScript(startScripts);
            startScripts.setEnabled(!extension.getOverlay().get());
        });

        SpringBootUtils.excludeDependenciesStarters(mainDistribution.getContents());

        javaExecFull.configure(run -> {
            ConfigurableFileCollection classpath = project.files();
            classpath.from(new File(installFullDist.get().getDestinationDir(), "lib/*"));
            run.setClasspath(classpath);

            run.getMainClass().convention(javaApplication.getMainClass());
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
                startScripts.setEnabled(!extension.getOverlay().getOrElse(false));

                startScripts.setUnixStartScriptGenerator(new GsbBootWarStartScriptGenerator());
                StartScriptUtil.disableWindowsScript(startScripts);
            });

            if (!extension.getOverlay().getOrElse(false)) {
                mainDistribution.contents(dist -> {
                    dist.into("bin", binSpec -> {
                        binSpec.from(createStartScriptsTaskProvider);
                        //noinspection OctalInteger
                        binSpec.setFileMode(0755);
                    });
                });
            }

        });

        project.getConfigurations().getByName(WarPlugin.PROVIDED_COMPILE_CONFIGURATION_NAME).extendsFrom(gsbComponent);

        Configuration gsbWar = project.getConfigurations().create("gsbWar");
        gsbWar.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.JAVA_RUNTIME));
        gsbWar.getAttributes().attribute(Bundling.BUNDLING_ATTRIBUTE, project.getObjects().named(Bundling.class, Bundling.EMBEDDED));
        gsbWar.getAttributes().attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, "war"));
        if (extension.getOverlay().get()) {
            gsbWar.getOutgoing().artifact(project.getTasks().named("war"));
        } else {
            gsbWar.getOutgoing().artifact(warTask);
        }
        javaComponent.addVariantsFromConfiguration(gsbWar, details -> {
        });

        javaExecFull.configure(run -> {
            ConfigurableFileCollection classpath = project.files();

            classpath.from(new File(installFullDist.get().getDestinationDir(), "WEB-INF/lib-provided/*"));
            classpath.from(new File(installFullDist.get().getDestinationDir(), "WEB-INF/lib/*"));
            classpath.from(new File(installFullDist.get().getDestinationDir(), "WEB-INF/classes"));

            run.setClasspath(classpath);

            run.getMainClass().convention(project.provider(() -> {
                File manifestFile = new File(installFullDist.get().getDestinationDir(), "META-INF/MANIFEST.MF");
                if (manifestFile.exists()) {
                    try (InputStream in = new FileInputStream(manifestFile)) {
                        return new Manifest(in).getMainAttributes().getValue("Start-Class");
                    }
                }
                return null;
            }));
        });

        project.afterEvaluate(p -> {
            p.getTasks().getByName("installDist").dependsOn(warTask);
            distZip.configure(task -> task.dependsOn(warTask));
            distTar.configure(task -> task.dependsOn(warTask));
            mainDistribution.contents(distContent -> {
                if (!extension.getOverlay().getOrElse(false)) {
                    distContent.from(p.zipTree(warTask.flatMap(War::getArchiveFile)), spec -> {
                        spec.exclude("org/springframework/boot/loader/**");
                        spec.setIncludeEmptyDirs(false);
                    });
                } else {
                    distContent.with(project.getTasks().named("war", War.class).get());
                }
            });

            if (extension.getOverlay().get()) {

                //IntelliJ verarschen, damit es die Pfade aus war-overlays auflöst.
                project.getTasks().withType(War.class, war -> {
                    CopySpecInternal childSpec = war.getRootSpec().addChild();

                    childSpec.from(extractComponents);
                    childSpec.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);

                    //noinspection Convert2Lambda
                    war.doFirst(new Action<Task>() {
                        @Override
                        public void execute(Task t) {
                            childSpec.exclude(element -> true);
                        }
                    });
                });

            }
        });
    }

    private void configureJib(Plugin<?> plugin) {
        JibExtension jibExtension = project.getExtensions().getByType(JibExtension.class);
        ContainerParameters container = jibExtension.getContainer();

        jibExtension.getFrom().setImage(JibUtil.getBaseImage(project));
        container.setCreationTime("USE_CURRENT_TIMESTAMP");

        container.getLabels().put("org.label-schema.schema-version", "1.0");

        String dateString = Instant.now().toString();
        container.getLabels().put("org.label-schema.build-date", dateString);
        container.getLabels().put("org.opencontainers.image.created", dateString);

        container.getLabels().put("org.label-schema.version", project.getVersion().toString());
        container.getLabels().put("org.opencontainers.image.version", project.getVersion().toString());

        try {
            Process execute = ProcessGroovyMethods.execute("git rev-parse HEAD");
            String gitSha = ProcessGroovyMethods.getText(execute).trim();

            container.getLabels().put("org.label-schema.vcs-ref", gitSha);
            container.getLabels().put("org.opencontainers.image.revision", gitSha);

        } catch (Exception e) {
            project.getLogger().warn(e.getLocalizedMessage(), e);
        }

        project.getPlugins().withType(WarPlugin.class, wp -> {
            project.getPlugins().withType(SpringBootPlugin.class, sbp -> {

                container.setAppRoot("/app");
                container.setEntrypoint(Arrays.asList(
                        "java",
                        "-cp",
                        "/app",
                        "org.springframework.boot.loader.WarLauncher"
                ));

            });
        });


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
