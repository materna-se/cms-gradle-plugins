package de.materna.cms.gradle.plugins.ci;

import org.gradle.api.provider.Property;

/**
 * Extension DSL für das {@link JavaLogsPlugin}.
 */
public abstract class JavaLogsExtension {

    /**
     * Das Basis-Directory welches in den Ausgaben von javac und javadoc entfernt wird, um relative Pfade zu erzeugen.
     * In der Regel der Jenkins Workspace oder der lokale Git working tree.
     */
    public abstract Property<String> getBaseDir();
}
