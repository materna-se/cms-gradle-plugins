package de.materna.cms.gradle.plugins.component;

import org.gradle.api.internal.plugins.DefaultTemplateBasedStartScriptGenerator;
import org.gradle.api.internal.plugins.UnixStartScriptGenerator;
import org.gradle.util.internal.TextUtil;

/**
 * @see UnixStartScriptGenerator
 */
public class CmsBootWarStartScriptGenerator extends DefaultTemplateBasedStartScriptGenerator {
    public CmsBootWarStartScriptGenerator() {
        super(TextUtil.getUnixLineSeparator(), CmsStartScriptTemplateBindingFactory.bootWar(), utf8ClassPathResource(UnixStartScriptGenerator.class, "unixStartScript.txt"));
    }
}
