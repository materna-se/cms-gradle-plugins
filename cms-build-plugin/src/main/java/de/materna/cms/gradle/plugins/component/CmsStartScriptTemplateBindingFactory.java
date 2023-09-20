package de.materna.cms.gradle.plugins.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.Transformer;
import org.gradle.api.internal.plugins.StartScriptTemplateBindingFactory;
import org.gradle.jvm.application.scripts.JavaAppStartScriptGenerationDetails;

import java.util.Map;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class CmsStartScriptTemplateBindingFactory implements Transformer<Map<String, String>, JavaAppStartScriptGenerationDetails> {

    private final String classpath;

    public static CmsStartScriptTemplateBindingFactory application() {
        return new CmsStartScriptTemplateBindingFactory("${GSB_LIB_DIR:-/opt/gsb/lib}/*:$APP_HOME/lib/*");
    }

    public static CmsStartScriptTemplateBindingFactory bootWar() {
        return new CmsStartScriptTemplateBindingFactory("${GSB_LIB_DIR:-/opt/gsb/lib}/*:$APP_HOME/WEB-INF/classes:$APP_HOME/WEB-INF/lib-provided/*:$APP_HOME/WEB-INF/lib/*");
    }

    @Override
    public Map<String, String> transform(JavaAppStartScriptGenerationDetails javaAppStartScriptGenerationDetails) {
        Map<String, String> base = StartScriptTemplateBindingFactory.unix().transform(javaAppStartScriptGenerationDetails);

        base.put("classpath", this.classpath);

        String mainClassName = base.get("mainClassName");
        if (mainClassName == null || mainClassName.isEmpty()) {
            log.warn("Keine Main-Klasse gesetzt.");
        }

        if (log.isInfoEnabled()) {
            base.entrySet().stream().map(Objects::toString).forEach(log::info);
        }

        return base;
    }
}
