package de.materna.gsb.gradle.plugins.component;

import org.gradle.api.internal.plugins.DefaultTemplateBasedStartScriptGenerator;
import org.gradle.api.internal.plugins.UnixStartScriptGenerator;
import org.gradle.util.internal.TextUtil;

/**
 * @see UnixStartScriptGenerator
 */
public class GsbBootWarStartScriptGenerator extends DefaultTemplateBasedStartScriptGenerator {
    public GsbBootWarStartScriptGenerator() {
        super(TextUtil.getUnixLineSeparator(), GsbStartScriptTemplateBindingFactory.bootWar(), utf8ClassPathResource(UnixStartScriptGenerator.class, "unixStartScript.txt"));
    }
}
