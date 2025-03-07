package de.materna.cms.gradle.plugins.component;

import com.google.cloud.tools.jib.gradle.ContainerParameters;
import com.google.cloud.tools.jib.gradle.JibExtension;
import com.google.cloud.tools.jib.gradle.JibTask;
import com.google.cloud.tools.jib.gradle.PlatformParameters;
import de.materna.cms.gradle.plugins.Util;
import de.materna.cms.gradle.plugins.WarLibraryPlugin;
import de.materna.cms.gradle.plugins.idea.IdeaUtils;
import de.materna.cms.gradle.plugins.jib.JibSemanticTagsPlugin;
import de.materna.cms.gradle.plugins.jib.JibUtil;
import de.materna.cms.gradle.plugins.sbom.SbomPlugin;
import org.codehaus.groovy.runtime.ProcessGroovyMethods;
import org.cyclonedx.gradle.CycloneDxTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
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
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.plugins.*;
import org.gradle.api.plugins.jvm.internal.JvmPluginServices;
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.jar.Manifest;

public abstract class CmsComponentPlugin implements Plugin<Project> {

    @Inject
    protected abstract JvmPluginServices getJvmPluginServices();

    @Inject
    protected abstract SoftwareComponentFactory getSoftwareComponentFactory();

    private Project project;
    private Distribution mainDistribution;
    private CmsComponentExtension extension;

    private TaskProvider<CreateStartScripts> createStartScriptsTaskProvider;
    private Configuration cmsComponent;

    private TaskProvider<Zip> distZip;
    private TaskProvider<Tar> distTar;
    private TaskProvider<Sync> extractComponents;

    private TaskProvider<Sync> installFullDist;
    private TaskProvider<Sync> syncCoreResources;
    private TaskProvider<Exec> execFull;
    private TaskProvider<JavaExec> javaExecFull;
    private AdhocComponentWithVariants cmsComponentSc;

    @Override
    public void apply(Project project) {
        this.project = project;
        project.getPlugins().apply(BasePlugin.class);
        extension = project.getExtensions().create("cmsComponent", CmsComponentExtension.class);

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

        cmsComponent = CmsComponentUtil.maybeCreateCmsComponentConfiguration(project);

        project.getArtifacts().add(cmsComponent.getName(), distZip);

        extractComponents = project.getTasks().register("extractComponents", Sync.class, task -> {
            task.setGroup("cms");
            task.into(project.getLayout().getBuildDirectory().dir("cms/components"));
            task.getInputs().files(cmsComponent);

            task.setDuplicatesStrategy(DuplicatesStrategy.WARN);

            for (File cmsComponent : cmsComponent) {
                task.from(project.zipTree(cmsComponent), Util::stripFirstPathSegment);
            }
        });

        Distribution fullDistribution = distributions.create("full");

        fullDistribution.getContents().from(extractComponents);
        fullDistribution.getContents().setDuplicatesStrategy(DuplicatesStrategy.WARN);
        fullDistribution.getContents().with(mainDistribution.getContents());

        project.getTasks().named("fullDistTar", Tar.class, tar -> tar.setEnabled(false));

        installFullDist = project.getTasks().named("installFullDist", Sync.class);

        execFull = project.getTasks().register("cmsRunFull", Exec.class, exec -> {
            exec.setGroup("cms");
            exec.setDescription("Startet die '" + extension.getName().getOrElse(project.getName()) + "' Komponente als Exec task.");
            exec.dependsOn(installFullDist);

            File binDir = new File(installFullDist.get().getDestinationDir(), "bin");
            exec.setExecutable(new File(binDir, extension.getName().get()));
        });
        javaExecFull = project.getTasks().register("cmsRunFullJava", JavaExec.class, exec -> {
            exec.setGroup("cms");
            exec.setDescription("Startet die '" + extension.getName().getOrElse(project.getName()) + "' Komponente als JavaExec task.");
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

                exec.systemProperty("java.io.tmpdir", exec.getTemporaryDir());
                exec.doFirst(new CreateTempDirAction());
            });
        });

        configureSoftwareComponents();

        project.getPlugins().withType(WarPlugin.class, this::configureWarComponent);
        project.getPlugins().withType(ApplicationPlugin.class, this::configureApplicationComponent);
        project.getPlugins().withId("org.springframework.boot", this::configureSpringBoot);

        project.getPlugins().withId("com.google.cloud.tools.jib", this::configureJib);

        project.getPlugins().withId("org.cyclonedx.bom", this::configureCycloneDx);

        project.getPlugins().apply(DuplicateStartScriptsPlugin.class);

        project.afterEvaluate(p -> {
            if (extension.getOverlay().get() && createStartScriptsTaskProvider != null) {
                createStartScriptsTaskProvider.configure(css -> css.setEnabled(false));
            }
        });

    }

    private void configureSoftwareComponents() {
        project.getPlugins().withType(JavaPlugin.class, jp -> {
            Util.getJavaSoftwareComponent(project).addVariantsFromConfiguration(cmsComponent, details -> {
            });
        });

        cmsComponentSc = getSoftwareComponentFactory().adhoc("cmsComponent");
        project.getComponents().add(cmsComponentSc);
        cmsComponentSc.addVariantsFromConfiguration(cmsComponent, details -> {
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

        project.getConfigurations().getByName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME).extendsFrom(cmsComponent);
        project.getConfigurations().getByName(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME).extendsFrom(cmsComponent);

        SpringBootUtils.excludeDependenciesStarters(mainDistribution.getContents());

        excludeCmsComponentApiDependenciesFromOverlayLibs();

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

    private void excludeCmsComponentApiDependenciesFromOverlayLibs() {
        Configuration cmsComponentJavaApi = project.getConfigurations().create("cmsComponentJavaApi");
        cmsComponentJavaApi.extendsFrom(cmsComponent);
        getJvmPluginServices().configureAsApiElements(cmsComponentJavaApi);

        mainDistribution.getContents().eachFile(fileCopyDetails -> {
            if (fileCopyDetails.getPath().startsWith("lib/") && cmsComponentJavaApi.contains(fileCopyDetails.getFile())) {
                if (extension.getOverlay().getOrElse(false)) {
                    fileCopyDetails.exclude();
                }
            }
        });
    }

    void configureWarComponent(WarPlugin warPlugin) {
        Property<War> warTask = project.getObjects().property(War.class);

        warTask.convention(project.getTasks().named("war", War.class));

        project.getPlugins().withId("java-library", jlp -> {
            project.getPlugins().apply(WarLibraryPlugin.class);
        });

        syncCoreResources = project.getTasks().register("syncCoreResources", Sync.class, task -> {
            task.dependsOn(extractComponents);
            task.setGroup("cms");
            task.setDescription("Synchronisiert die Kerndateien aus dem WEB-INF Ordner der CAE/Site in den Mandanten.");
            task.from(extractComponents.get().getDestinationDir() + "/WEB-INF", (CopySpec copySpec) -> {
                copySpec.include("templates/");
                copySpec.include("tld/");
            });
            task.preserve(filterable -> filterable.include("templates/customers/"));
            TaskProvider<War> war = project.getTasks().named("war", War.class);
            task.into(war.get().getWebAppDirectory().dir("WEB-INF"));
        });

        project.getPlugins().withId("org.springframework.boot", sbp -> {
            TaskProvider<BootWar> bootWar = project.getTasks().named("bootWar", BootWar.class);
            warTask.set(bootWar);

            createStartScriptsTaskProvider = project.getTasks().register(ApplicationPlugin.TASK_START_SCRIPTS_NAME, CreateStartScripts.class, startScripts -> {
                startScripts.getMainClass().set(bootWar.flatMap(BootWar::getMainClass));
                startScripts.setOutputDir(project.getLayout().getBuildDirectory().dir("cmsScripts").get().getAsFile());
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
                        binSpec.filePermissions(cfp -> cfp.unix(0755));
                    });
                });
            }

        });

        project.getConfigurations().getByName(WarPlugin.PROVIDED_COMPILE_CONFIGURATION_NAME).extendsFrom(cmsComponent);

        Configuration cmsWar = project.getConfigurations().create("cmsWar");
        cmsWar.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.JAVA_RUNTIME));
        cmsWar.getAttributes().attribute(Bundling.BUNDLING_ATTRIBUTE, project.getObjects().named(Bundling.class, Bundling.EMBEDDED));
        cmsWar.getAttributes().attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, "war"));
        if (extension.getOverlay().get()) {
            cmsWar.getOutgoing().artifact(project.getTasks().named("war"));
        } else {
            cmsWar.getOutgoing().artifact(warTask);
        }

        Util.getJavaSoftwareComponent(project).addVariantsFromConfiguration(cmsWar, details -> {
        });

        javaExecFull.configure(run -> {
            ConfigurableFileCollection classpath = project.files();

            classpath.from(new File(installFullDist.get().getDestinationDir(), "WEB-INF/classes"));
            classpath.from(new File(installFullDist.get().getDestinationDir(), "WEB-INF/lib-provided/*"));
            classpath.from(new File(installFullDist.get().getDestinationDir(), "WEB-INF/lib/*"));

            run.setClasspath(classpath);

            run.getMainClass().convention(findMainClassFromDist(installFullDist));
        });

        project.afterEvaluate(p -> {
            p.getTasks().named("installDist").configure(installDist -> installDist.dependsOn(warTask));
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
                p.getPlugins().withId("org.springframework.boot", plugin -> {
                    p.getTasks().named("bootWar", BootWar.class).configure(task -> {
                        task.dependsOn(extractComponents);
                        task.getMainClass().convention(findMainClassFromDist(extractComponents));
                    });
                });


                //IntelliJ verarschen, damit es die Pfade aus war-overlays auflöst.
                if (IdeaUtils.isIntellJSync(project)) {

                    project.getTasks().withType(War.class, war -> {
                        war.from(extractComponents);
                    });
                }
            }

        });
    }

    private Provider<String> findMainClassFromDist(TaskProvider<Sync> taskProvider) {
        return taskProvider.map(sync -> {
            File manifestFile = new File(sync.getDestinationDir(), "META-INF/MANIFEST.MF");
            if (manifestFile.exists()) {
                try (InputStream in = Files.newInputStream(manifestFile.toPath())) {
                    String startClass = new Manifest(in).getMainAttributes().getValue("Start-Class");
                    if (startClass == null) {
                        project.getLogger().warn("Kein Start-Class in {} gefunden", manifestFile);
                    } else {
                        return startClass;
                    }
                } catch (IOException e) {
                    sync.getLogger().warn("Fehler beim Lesen von {}", manifestFile, e);
                }
            }
            return null;
        });
    }

    private void configureJib(Plugin<?> plugin) {
        JibExtension jibExtension = project.getExtensions().getByType(JibExtension.class);
        ContainerParameters container = jibExtension.getContainer();

        if (jibExtension.getFrom().getImage() == null) {
            jibExtension.getFrom().setImage(JibUtil.getBaseImage(project));
        }

        project.getPlugins().apply(JibSemanticTagsPlugin.class);

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

        container.getLabels().put("de.materna.cms.from-image", project.provider(() -> jibExtension.getFrom().getImage()));

        try {
            Process execute = ProcessGroovyMethods.execute("git rev-parse HEAD");
            String gitSha = ProcessGroovyMethods.getText(execute).trim();

            container.getLabels().put("org.label-schema.vcs-ref", gitSha);
            container.getLabels().put("org.opencontainers.image.revision", gitSha);

        } catch (Exception e) {
            project.getLogger().warn(e.getLocalizedMessage(), e);
        }

        project.getPlugins().withType(WarPlugin.class, wp -> {
            container.setAppRoot("/app");
            project.getPlugins().withId("org.springframework.boot", sbp -> {
                configureJibForSpringBootWar(container);
            });
        });

        project.getPlugins().withType(ApplicationPlugin.class, applicationPlugin -> configureJibForApplication(jibExtension));

    }

    private void configureJibForSpringBootWar(ContainerParameters container) {
        container.setAppRoot("/app");

        project.afterEvaluate(p -> {

            if (container.getEntrypoint() == null || container.getEntrypoint().isEmpty()) {

                if (extension.getOverlay().getOrElse(false)) {
                    container.setEntrypoint("INHERIT");
                } else {
                    TaskProvider<BootWar> bootWar = project.getTasks().named(SpringBootPlugin.BOOT_WAR_TASK_NAME, BootWar.class);

                    TaskProvider<Task> configureEntryPoint = project.getTasks().register("configureJibBootWarEntrypoint", conf -> {
                        conf.mustRunAfter(bootWar);
                        conf.doLast(t -> {
                            List<String> entrypoint = new ArrayList<>();
                            entrypoint.add("java");

                            if (container.getJvmFlags() != null) {
                                entrypoint.addAll(container.getJvmFlags());
                            }

                            entrypoint.add("-cp");
                            entrypoint.add("/app/WEB-INF/classes:/app/WEB-INF/lib-provided/*:/app/WEB-INF/lib/*");

                            entrypoint.add(bootWar.get().getMainClass().get());

                            container.setEntrypoint(entrypoint);
                        });
                    });

                    project.getTasks().withType(JibTask.class).configureEach(jibTask -> jibTask.dependsOn(configureEntryPoint));
                }
            }
        });
    }

    private void configureJibForApplication(JibExtension jibExtension) {
        project.afterEvaluate(p -> {
            JavaApplication javaApplication = project.getExtensions().getByType(JavaApplication.class);

            ContainerParameters container = jibExtension.getContainer();

            if (container.getMainClass() == null && javaApplication.getMainClass().isPresent()) {
                container.setMainClass(javaApplication.getMainClass().get());
            }

            if (container.getEntrypoint() == null || container.getEntrypoint().isEmpty())
                if (extension.getOverlay().getOrElse(false)) {
                    container.setEntrypoint("INHERIT");
                } else {
                    List<String> entrypoint = new ArrayList<>();
                    entrypoint.add("java");

                    if (container.getJvmFlags() != null) {
                        entrypoint.addAll(container.getJvmFlags());
                    }

                    entrypoint.add("-cp");
                    entrypoint.add("/app/resources:/app/classes:/app/libs/*");

                    entrypoint.add("@/app/jib-main-class-file");

                    container.setEntrypoint(entrypoint);
                }

            container.setExpandClasspathDependencies(false);

            if (!container.getEnvironment().containsKey("JAVA_TOOL_OPTIONS")) {
                String javaOpts = String.join(" ", javaApplication.getApplicationDefaultJvmArgs());

                if (!javaOpts.isEmpty()) {
                    if (container.getEnvironment().isEmpty()) {
                        container.setEnvironment(new HashMap<>());
                    }
                    container.getEnvironment().put("JAVA_TOOL_OPTIONS", javaOpts);
                }
            }

        });
    }

    private void configureCycloneDx(Plugin<?> plugin) {

        project.getPlugins().apply(SbomPlugin.class);

        TaskProvider<CycloneDxTask> cyclonedxBomTask = project.getTasks().named("cyclonedxBom", CycloneDxTask.class);

        //Erst aus machen, und dann für War und Application wieder an machen.
        cyclonedxBomTask.configure(cyclonedxBom -> {
            cyclonedxBom.setEnabled(false);

            cyclonedxBom.getOutputName().convention(extension.getName().map(name -> String.format("%s-%s.cdx", name, project.getVersion())));
        });

        project.getPlugins().withType(WarPlugin.class, warPlugin -> {
            cyclonedxBomTask.configure(cyclonedxBom -> {
                cyclonedxBom.setEnabled(true);
                cyclonedxBom.getProjectType().convention("application");
                cyclonedxBom.getIncludeConfigs().convention(Collections.singletonList(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME));
                cyclonedxBom.getSkipConfigs().convention(Arrays.asList(WarPlugin.PROVIDED_COMPILE_CONFIGURATION_NAME, WarPlugin.PROVIDED_RUNTIME_CONFIGURATION_NAME));
            });
        });

        project.getPlugins().withType(ApplicationPlugin.class, applicationPlugin -> {
            cyclonedxBomTask.configure(cyclonedxBom -> {
                cyclonedxBom.setEnabled(true);
                cyclonedxBom.getProjectType().convention("application");
                cyclonedxBom.getIncludeConfigs().convention(Collections.singletonList(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME));
            });
        });

        cmsComponentSc.addVariantsFromConfiguration(project.getConfigurations().getByName(SbomPlugin.SBOM_CONFIGURATION), details -> {
        });
    }
}
