package de.materna.cms.gradle.plugins.docker;


import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

public abstract class DuplicateDockerImagesExtension {

    public abstract Property<String> getVersion();

    public abstract Property<String> getSourceRegistry();

    public abstract ListProperty<String> getImageNames();

    public abstract ListProperty<String> getTargetRegistries();
}
