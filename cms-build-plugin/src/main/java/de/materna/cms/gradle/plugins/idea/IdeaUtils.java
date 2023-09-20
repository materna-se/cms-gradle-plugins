package de.materna.cms.gradle.plugins.idea;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

import java.io.File;

public class IdeaUtils {

    public static boolean isIntellJSync(Project project) {

        Provider<String> ideaSyncActive = project.getProviders().systemProperty("idea.sync.active");

        if (ideaSyncActive.isPresent() && "true".equalsIgnoreCase(ideaSyncActive.get())) {
            return true;
        }

        for (File initScript : project.getGradle().getStartParameter().getAllInitScripts()) {
            if (initScript.getName().startsWith("sync.studio.tooling")) {
                return true;
            }

            if (initScript.getName().equals("ijInit1.gradle")) {
                return true;
            }
        }

        return false;
    }
}
