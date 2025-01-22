package de.materna.cms.gradle.plugins.docker;


import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.TaskProvider;

import java.util.HashMap;
import java.util.Map;

public class DuplicateDockerImagesPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {


        DuplicateDockerImagesExtension extension = project.getExtensions().create("duplicateDockerImages", DuplicateDockerImagesExtension.class);
        extension.getVersion().convention(project.getVersion().toString());

        TaskProvider<Task> duplicateDockerImages = project.getTasks().register("duplicateDockerImages");
        duplicateDockerImages.configure(t -> {
            t.setGroup("docker");
        });

        Map<String, TaskProvider<Task>> pushPerRegistry = new HashMap<>();

        project.afterEvaluate(p -> {

            for (String imageName : extension.getImageNames().get()) {

                String fullSourceImage = getImageName(extension.getSourceRegistry().get(), imageName, extension.getVersion().get());
                TaskProvider<Exec> pullTask = project.getTasks().register("dockerPull__" + cleanTaskName(imageName), Exec.class);
                pullTask.configure(t -> {
                    t.setGroup("docker");

                    t.setDescription("Pull image " + fullSourceImage);

                    t.executable("docker");
                    t.args("pull", fullSourceImage);
                });

                for (String targetRegistry : extension.getTargetRegistries().get()) {

                    String targetName = getImageName(targetRegistry, imageName, null);
                    String fullTargetName = getImageName(targetRegistry, imageName, extension.getVersion().get());

                    TaskProvider<Exec> tagTask = project.getTasks().register("dockerTag__" + cleanTaskName(targetName), Exec.class);
                    tagTask.configure(t -> {
                        t.setGroup("docker");
                        t.dependsOn(pullTask);
                        t.setDescription("Tag image " + fullSourceImage + " as " + fullTargetName);

                        t.executable("docker");
                        t.args("tag");
                        t.args(fullSourceImage);
                        t.args(fullTargetName);

                    });

                    TaskProvider<Exec> pushTask = project.getTasks().register("dockerPush__" + cleanTaskName(targetName), Exec.class);
                    pushTask.configure(t -> {
                        t.setGroup("docker");
                        t.dependsOn(tagTask);
                        t.setDescription("Push image " + fullTargetName);

                        t.executable("docker");
                        t.args("push");
                        t.args(fullTargetName);
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
