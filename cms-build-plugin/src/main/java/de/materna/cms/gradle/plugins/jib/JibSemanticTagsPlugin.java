package de.materna.cms.gradle.plugins.jib;

import com.google.cloud.tools.jib.gradle.JibExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class JibSemanticTagsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().withId("com.google.cloud.tools.jib", plugin -> configureJibTags(project));
    }

    private void configureJibTags(Project project) {
        JibExtension jib = project.getExtensions().getByType(JibExtension.class);

        jib.getTo().setTags(project.provider(() -> resolveTags(project.getVersion().toString())));
    }

    public static Set<String> resolveTags(String version) {
        if (version.endsWith("-SNAPSHOT")) {
            return Collections.singleton(version);
        }

        Set<String> tags = new LinkedHashSet<>();

        int i = version.indexOf(".");

        while (i != -1 && i < version.length())  {
            String tag = version.substring(0, i);
            tags.add(tag);
            i = version.indexOf(".", i+1);
        }

        tags.add(version);

        return tags;
    }
}
