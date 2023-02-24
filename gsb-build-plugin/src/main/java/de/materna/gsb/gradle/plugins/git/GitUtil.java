package de.materna.gsb.gradle.plugins.git;

import lombok.experimental.UtilityClass;

import java.io.IOException;

import static org.codehaus.groovy.runtime.ProcessGroovyMethods.execute;
import static org.codehaus.groovy.runtime.ProcessGroovyMethods.getText;

@UtilityClass
public class GitUtil {


    public static boolean isGitDirty() throws IOException {
        return getText(execute("git describe --dirty --always")).trim().endsWith("-dirty");
    }

    public static boolean isGitConflict() throws IOException {
        return !getText(execute("git ls-files -u")).trim().isEmpty();
    }

    public static String getCurrentGitHash() throws IOException {
        return getText(execute("git rev-parse -q HEAD")).trim();
    }
}
