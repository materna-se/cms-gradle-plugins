package de.materna.cms.gradle.plugins.component;

import com.google.cloud.tools.jib.gradle.ContainerParameters;
import com.google.cloud.tools.jib.gradle.JibExtension;
import com.google.cloud.tools.jib.gradle.PlatformParameters;
import de.materna.cms.gradle.plugins.Util;
import de.materna.cms.gradle.plugins.WarLibraryPlugin;
import org.codehaus.groovy.runtime.ProcessGroovyMethods;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.distribution.Distribution;
import org.gradle.api.distribution.DistributionContainer;
import org.gradle.api.distribution.plugins.DistributionPlugin;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.internal.file.copy.CopySpecInternal;
import org.gradle.api.plugins.*;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.application.CreateStartScripts;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Tar;
import org.gradle.api.tasks.bundling.War;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.jvm.toolchain.JavaToolchainSpec;
import org.springframework.boot.gradle.plugin.SpringBootPlugin;
import org.springframework.boot.gradle.tasks.bundling.BootWar;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.Manifest;

public class CmsComponentPlugin implements Plugin<Project> {


    private final SoftwareComponentFactory softwareComponentFactory;

    private Project project;
    private Distribution mainDistribution;
    private CmsComponentExtension extension;

    private TaskProvider<CreateStartScripts> createStartScriptsTaskProvider;
    private Configuration gsbComponent;

    private TaskProvider<Zip> distZip;
    private TaskProvider<Tar> distTar;
    private TaskProvider<Sync> extractComponents;

    private TaskProvider<Sync> installFullDist;
    private TaskProvider<Exec> execFull;
    private TaskProvider<JavaExec> javaExecFull;

    @Inject
    public CmsComponentPlugin(SoftwareComponentFactory softwareComponentFactory) {
        this.softwareComponentFactory = softwareComponentFactory;
    }

    @Override
    public void apply(Project project) {
        this.project = project;
        project.getPlugins().apply(BasePlugin.class);
        extension = project.getExtensions().create("gsbComponent", CmsComponentExtension.class);

        BasePluginExtension basePluginExtension = project.getExtensions().getByType(BasePluginExtension.class);

        extension.getName().convention(basePluginExtension.getArchivesName());

        project.getPlugins().apply(DistributionPlugin.class);

        DistributionContainer distributions = project.getExtensions().getByType(DistributionContainer.class);
        mainDistribution = distributions.getByName("main");

        mainDistribution.getDistributionBaseName().set(extension.getName());

        distZip = project.getTasks().named("distZip", Zip.class);
        distTar = project.getTasks().named("distTar", Tar.class, tar -> tar.setEnabled(false));

        distZip.configure(task -> task.getArchiveVersion().set(""));
        distTar.configure(task -> task.getArchiveVersion().set(""));

        gsbComponent = CmsComponentUtil.maybeCreateGsbComponentConfiguration(project);

        project.getArtifacts().add(gsbComponent.getName(), distZip);

        extractComponents = project.getTasks().register("extractComponents", Sync.class, task -> {
            task.setGroup("gsb");
            task.into(project.getLayout().getBuildDirectory().dir("gsb/components"));
            task.getInputs().files(gsbComponent);

            task.setDuplicatesStrategy(DuplicatesStrategy.WARN);

            for (File gsbComponent : gsbComponent) {
                task.from(project.zipTree(gsbComponent), Util::stripFirstPathSegment);
            }
        });

        Distribution fullDistribution = distributions.create("full");

        fullDistribution.getContents().from(extractComponents);
        fullDistribution.getContents().setDuplicatesStrategy(DuplicatesStrategy.WARN);
        fullDistribution.getContents().with(mainDistribution.getContents());

        project.getTasks().named("fullDistTar", Tar.class, tar -> tar.setEnabled(false));

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
            exec.setEnabled(false);
        });

        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
            javaExecFull.configure(exec -> {
                exec.setEnabled(true);

                JavaToolchainSpec defaultToolchain = project.getExtensions().getByType(JavaPluginExtension.class).getToolchain();

                JavaToolchainService javaToolchainService = project.getExtensions().getByType(JavaToolchainService.class);
                Provider<JavaLauncher> defaultJavaLauncher = javaToolchainService.launcherFor(defaultToolchain);

                exec.getJavaLauncher().convention(defaultJavaLauncher);
            });
        });

        configureSoftwareComponents();

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

    private void configureSoftwareComponents() {
        project.getPlugins().withType(JavaPlugin.class, jp -> {
            Util.getJavaSoftwareComponent(project).addVariantsFromConfiguration(gsbComponent, details -> {
            });
        });

        AdhocComponentWithVariants gsbComponentSc = softwareComponentFactory.adhoc("gsbComponent");
        project.getComponents().add(gsbComponentSc);
        gsbComponentSc.addVariantsFromConfiguration(gsbComponent, details -> {
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
            startScripts.setUnixStartScriptGenerator(new CmsApplicationStartScriptGenerator());
            StartScriptUtil.disableWindowsScript(startScripts);
            startScripts.setEnabled(!extension.getOverlay().get());
        });

        project.getConfigurations().getByName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME).extendsFrom(gsbComponent);
        project.getConfigurations().getByName(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME).extendsFrom(gsbComponent);

        SpringBootUtils.excludeDependenciesStarters(mainDistribution.getContents());

        project.getPlugins().withId("org.springframework.boot", sbp -> {
            project.getTasks().named("bootDistTar", Tar.class, tar -> tar.setEnabled(false));
        });

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

        project.getPlugins().withId("java-library", jlp -> {
            project.getPlugins().apply(WarLibraryPlugin.class);
        });

        project.getPlugins().withId("org.springframework.boot", sbp -> {
            TaskProvider<BootWar> bootWar = project.getTasks().named("bootWar", BootWar.class);
            warTask.set(bootWar);

            createStartScriptsTaskProvider = project.getTasks().register(ApplicationPlugin.TASK_START_SCRIPTS_NAME, CreateStartScripts.class, startScripts -> {
                startScripts.getMainClass().set(bootWar.flatMap(BootWar::getMainClass));
                startScripts.setOutputDir(project.getLayout().getBuildDirectory().dir("gsbScripts").get().getAsFile());
                startScripts.setApplicationName(extension.getName().get());
                startScripts.setEnabled(!extension.getOverlay().getOrElse(false));

                startScripts.setUnixStartScriptGenerator(new CmsBootWarStartScriptGenerator());
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

        Util.getJavaSoftwareComponent(project).addVariantsFromConfiguration(gsbWar, details -> {
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
                        String startClass = new Manifest(in).getMainAttributes().getValue("Start-Class");
                        if (startClass == null) {
                            run.getLogger().warn("Kein Start-Class in {} gefunden", manifestFile);
                        } else {
                            return startClass;
                        }
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
                distContent.from(p.zipTree(warTask.flatMap(War::getArchiveFile)), spec -> {
                    spec.exclude("org/springframework/boot/loader/**");

                    if (extension.getOverlay().getOrElse(false)) {
                        spec.exclude("META-INF/MANIFEST.MF");
                    }
                    spec.setIncludeEmptyDirs(false);
                });
            });

            if (extension.getOverlay().get()) {

                //IntelliJ verarschen, damit es die Pfade aus war-overlays auflöst.
                project.getTasks().withType(War.class, war -> {
                    CopySpecInternal childSpec = war.getRootSpec().addChild();

                    childSpec.from(extractComponents);
                    childSpec.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);

                    boolean isIntelliJSync = project.getGradle().getStartParameter().getAllInitScripts().stream()
                            .anyMatch(initScript -> initScript.getName().equals("ijMapper1.gradle"));

                    childSpec.exclude(element -> !isIntelliJSync);
                });

            }
        });
    }

    private void configureJib(Plugin<?> plugin) {
        JibExtension jibExtension = project.getExtensions().getByType(JibExtension.class);
        ContainerParameters container = jibExtension.getContainer();

        if (jibExtension.getFrom().getImage() == null) {
            jibExtension.getFrom().setImage(JibUtil.getBaseImage(project));
        }

        PlatformParameters platformParameters = new PlatformParameters();
        platformParameters.setArchitecture("amd64");
        platformParameters.setOs("linux");
        jibExtension.getFrom().getPlatforms().convention(Collections.singletonList(platformParameters));

        container.getCreationTime().convention("USE_CURRENT_TIMESTAMP");

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
                        "/app/WEB-INF/classes:/app/WEB-INF/lib-provided/*:/app/WEB-INF/lib/*",
                        "@/app/jib-main-class-file"
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
