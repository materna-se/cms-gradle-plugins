package de.bund.gsb.gradle.plugins.component;


import lombok.Getter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

@Getter
public class GsbComponentExtension {

    private Property<String> name;
    @Inject
    public GsbComponentExtension(ObjectFactory objectFactory) {
        name = objectFactory.property(String.class);
    }
}
