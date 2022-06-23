package de.bund.gsb.gradle.plugins.maven;

import io.freefair.gradle.plugins.compress.tasks.SevenZip;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.file.Directory;
import org.gradle.api.internal.artifacts.BaseRepositoryFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Zip;

import javax.inject.Inject;

public class MavenRepositoryPlugin implements Plugin<Project> {

    private final BaseRepositoryFactory repositoryFactory;

    private Provider<Directory> repoDir;
    private MavenArtifactRepository mavenRepository;
    private MavenPublication repositoryPublication;
    private TaskProvider<Delete> cleanLocalMavenRepo;
    private TaskProvider<Task> publishAllTask;


    @Inject
    public MavenRepositoryPlugin(BaseRepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }


    @Override
    public void apply(Project project) {

        repoDir = project.getLayout().getBuildDirectory().dir("maven-repo");

        mavenRepository = repositoryFactory.createMavenRepository();
        mavenRepository.setUrl(repoDir);
        mavenRepository.setName("localTemp");

        cleanLocalMavenRepo = project.getTasks().register("cleanLocalMavenRepo", Delete.class, task -> {
            task.delete(repoDir);
        });

        publishAllTask = project.getTasks().register("publishToLocalMavenRepo", task -> {
            task.setGroup("publishing");
        });

        TaskProvider<Zip> mavenRepoZip = project.getTasks().register("mavenRepoZip", Zip.class, zipTask -> {
            zipTask.from(repoDir);
            zipTask.dependsOn(publishAllTask);
            zipTask.setZip64(true);
            zipTask.getArchiveAppendix().set("maven-repository");
            zipTask.getDestinationDirectory().convention(project.getLayout().getBuildDirectory());
        });

        project.getPlugins().apply("maven-publish");

        PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);

        repositoryPublication = publishing.getPublications().create("mavenRepo", MavenPublication.class, mrp -> {
            mrp.setArtifactId(project.getName() + "-mavenr-repository");
            mrp.artifact(mavenRepoZip);
        });

        project.getTasks().withType(PublishToMavenRepository.class).configureEach(ptmr -> {
            ptmr.onlyIf(t -> !(ptmr.getRepository() == mavenRepository && ptmr.getPublication() == repositoryPublication));
        });

        project.allprojects(this::configureProject);
    }

    private void configureProject(Project project) {
        project.getPlugins().withType(MavenPublishPlugin.class, mpp -> {
            PublishingExtension publishingExtension = project.getExtensions().findByType(PublishingExtension.class);

            publishingExtension.getRepositories().add(mavenRepository);

            publishAllTask.configure(pat -> {
                pat.dependsOn(project.getTasks().withType(PublishToMavenRepository.class)
                        .matching(ptmr -> ptmr.getRepository() == mavenRepository && ptmr.getPublication() != repositoryPublication));
            });

        });

        project.afterEvaluate(sp -> {
            project.getTasks().withType(PublishToMavenRepository.class).configureEach(ptmr -> {
                if (ptmr.getRepository() == mavenRepository) {
                    ptmr.dependsOn(cleanLocalMavenRepo);
                }
            });
        });
    }
}
