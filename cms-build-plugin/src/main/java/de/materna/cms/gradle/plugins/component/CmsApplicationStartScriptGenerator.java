package de.materna.cms.gradle.plugins.component;

import org.gradle.api.internal.plugins.DefaultTemplateBasedStartScriptGenerator;
import org.gradle.api.internal.plugins.UnixStartScriptGenerator;
import org.gradle.util.internal.TextUtil;

/**
 * @see UnixStartScriptGenerator
 */
public class CmsApplicationStartScriptGenerator extends DefaultTemplateBasedStartScriptGenerator {
    public CmsApplicationStartScriptGenerator() {
        super(TextUtil.getUnixLineSeparator(), CmsStartScriptTemplateBindingFactory.application(), utf8ClassPathResource(UnixStartScriptGenerator.class, "unixStartScript.txt"));
    }
}
