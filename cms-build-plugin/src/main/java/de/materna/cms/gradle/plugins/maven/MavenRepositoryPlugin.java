/*
 * Copyright 2025 Materna Information & Communications SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.materna.cms.gradle.plugins.maven;

import lombok.Getter;
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

@Getter
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

        cleanLocalMavenRepo = project.getTasks().register("cleanLocalTempRepository", Delete.class, task -> {
            task.setGroup("publishing");
            task.delete(repoDir);
        });

        publishAllTask = project.getTasks().register("publishToLocalTempRepository", task -> {
            task.setGroup("publishing");
            task.setDescription("Publiziert alle Projekte in das lokale temporäre Repository");
            task.dependsOn(cleanLocalMavenRepo);
        });

        TaskProvider<Zip> mavenRepoZip = project.getTasks().register("mavenRepoZip", Zip.class, task -> {
            task.setGroup("publishing");
            task.from(repoDir);
            task.dependsOn(publishAllTask);
            task.setZip64(true);
            task.getArchiveAppendix().set("maven-repository");
            task.getDestinationDirectory().convention(project.getLayout().getBuildDirectory());
        });

        project.getPlugins().apply("maven-publish");

        PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);

        repositoryPublication = publishing.getPublications().create("mavenRepo", MavenPublication.class, mrp -> {
            mrp.setArtifactId(mrp.getArtifactId() + "-maven-repository");
            mrp.artifact(mavenRepoZip);
        });

        project.getTasks().withType(PublishToMavenRepository.class).configureEach(ptmr -> {
            ptmr.onlyIf(t -> !(ptmr.getRepository() == mavenRepository && ptmr.getPublication() == repositoryPublication));
        });

        project.allprojects(this::configureProject);
    }

    private void configureProject(Project project) {
        project.getPlugins().withType(MavenPublishPlugin.class, mpp -> {
            PublishingExtension publishingExtension = project.getExtensions().getByType(PublishingExtension.class);

            publishingExtension.getRepositories().add(mavenRepository);

            publishAllTask.configure(pat -> {
                pat.dependsOn(project.getTasks().withType(PublishToMavenRepository.class)
                        .matching(ptmr -> ptmr.getRepository() == mavenRepository && ptmr.getPublication() != repositoryPublication));
            });

            project.getTasks().withType(PublishToMavenRepository.class).configureEach(ptmr -> {
                ptmr.mustRunAfter(cleanLocalMavenRepo);
                if (ptmr.getRepository() == mavenRepository) {
                    ptmr.dependsOn(cleanLocalMavenRepo);
                }
            });
        });
    }
}
