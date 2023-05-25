package de.materna.cms.gradle.plugins.component;


import lombok.Getter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

@Getter
public class GsbComponentExtension {

    private Property<String> name;

    private Property<Boolean> overlay;
    @Inject
    public GsbComponentExtension(ObjectFactory objectFactory) {
        name = objectFactory.property(String.class);
        overlay = objectFactory.property(Boolean.class).convention(false);
    }
}
