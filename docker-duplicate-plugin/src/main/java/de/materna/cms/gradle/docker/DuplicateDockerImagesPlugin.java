package de.materna.cms.gradle.docker;


import com.bmuschko.gradle.docker.DockerRemoteApiPlugin;
import com.bmuschko.gradle.docker.tasks.image.DockerPullImage;
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage;
import com.bmuschko.gradle.docker.tasks.image.DockerTagImage;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

import java.util.HashMap;
import java.util.Map;

public class DuplicateDockerImagesPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getPlugins().apply(DockerRemoteApiPlugin.class);

        DuplicateDockerImagesExtension extension = project.getExtensions().create("duplicateDockerImages", DuplicateDockerImagesExtension.class);
        extension.getVersion().convention(project.getVersion().toString());

        TaskProvider<Task> duplicateDockerImages = project.getTasks().register("duplicateDockerImages");
        duplicateDockerImages.configure(t -> {
            t.setGroup("docker");
        });

        Map<String, TaskProvider<Task>> pushPerRegistry = new HashMap<>();

        project.afterEvaluate(p -> {

            for (String imageName : extension.getImageNames().get()) {

                TaskProvider<DockerPullImage> pullTask = project.getTasks().register("dockerPull__" + cleanTaskName(imageName), DockerPullImage.class);
                pullTask.configure(t -> {
                    t.setGroup("docker");

                    String fullImageName = getImageName(extension.getSourceRegistry().get(), imageName, extension.getVersion().get());
                    t.getImage().set(fullImageName);
                    t.setDescription("Pull image " + fullImageName);
                });

                for (String targetRegistry : extension.getTargetRegistries().get()) {

                    String targetName = getImageName(targetRegistry, imageName, null);

                    TaskProvider<DockerTagImage> tagTask = project.getTasks().register("dockerTag__" + cleanTaskName(targetName), DockerTagImage.class);
                    tagTask.configure(t -> {
                        t.setGroup("docker");
                        t.getImageId().convention(pullTask.flatMap(DockerPullImage::getImage));
                        t.dependsOn(pullTask);
                        String targetImageName = getImageName(targetRegistry, imageName, extension.getVersion().get());
                        t.setDescription("Tag image " + pullTask.get().getImage().get() + " as " + targetImageName);

                        t.getTag().set(targetImageName);
                        t.getRepository().set("");
                    });

                    TaskProvider<DockerPushImage> pushTask = project.getTasks().register("dockerPush__" + cleanTaskName(targetName), DockerPushImage.class);
                    pushTask.configure(t -> {
                        t.setGroup("docker");
                        t.dependsOn(tagTask);
                        t.getImages().add(tagTask.flatMap(DockerTagImage::getTag));

                        t.setDescription("Push image " + tagTask.get().getTag().get());
                    });

                    pushPerRegistry.computeIfAbsent(targetRegistry, tN -> {
                                TaskProvider<Task> pushAllToRegistry = project.getTasks().register("dockerPush__" + cleanTaskName(targetRegistry));
                                pushAllToRegistry.configure(t -> {
                                    t.setGroup("docker");
                                    t.setDescription("Push all images to registry " + targetRegistry);
                                });
                                return pushAllToRegistry;
                            })
                            .configure(t -> t.dependsOn(pushTask));

                    duplicateDockerImages.configure(t -> t.dependsOn(pushTask));

                }

            }
        });
    }

    public static String getImageName(String registry, String imageName, String version) {

        String result = imageName;

        if (registry != null) {
            result = registry + "/" + result;
        }

        if (version != null) {
            result = result + ":" + version;
        }

        return result;
    }

    public static String cleanTaskName(String taskName) {
        return taskName.replace("/", "_")
                .replace(":", "_");
    }
}
