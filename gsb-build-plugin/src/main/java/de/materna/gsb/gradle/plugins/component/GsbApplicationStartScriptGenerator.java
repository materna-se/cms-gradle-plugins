package de.materna.gsb.gradle.plugins.component;

import org.gradle.api.internal.plugins.DefaultTemplateBasedStartScriptGenerator;
import org.gradle.api.internal.plugins.UnixStartScriptGenerator;
import org.gradle.util.internal.TextUtil;

/**
 * @see UnixStartScriptGenerator
 */
public class GsbApplicationStartScriptGenerator extends DefaultTemplateBasedStartScriptGenerator {
    public GsbApplicationStartScriptGenerator() {
        super(TextUtil.getUnixLineSeparator(), GsbStartScriptTemplateBindingFactory.application(), utf8ClassPathResource(UnixStartScriptGenerator.class, "unixStartScript.txt"));
    }
}
