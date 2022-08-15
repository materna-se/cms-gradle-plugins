package de.bund.gsb.gradle.plugins.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.Transformer;
import org.gradle.api.internal.plugins.StartScriptTemplateBindingFactory;
import org.gradle.jvm.application.scripts.JavaAppStartScriptGenerationDetails;

import java.util.Map;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class GsbStartScriptTemplateBindingFactory implements Transformer<Map<String, String>, JavaAppStartScriptGenerationDetails> {

    private final String classpath;

    public static GsbStartScriptTemplateBindingFactory application() {
        return new GsbStartScriptTemplateBindingFactory("${GSB_LIB_DIR:-/opt/gsb/lib}/*:$APP_HOME/lib/*");
    }

    public static GsbStartScriptTemplateBindingFactory bootWar() {
        return new GsbStartScriptTemplateBindingFactory("${GSB_LIB_DIR:-/opt/gsb/lib}/*:$APP_HOME/WEB-INF/classes:$APP_HOME/WEB-INF/lib-provided/*:$APP_HOME/WEB-INF/lib/*");
    }

    @Override
    public Map<String, String> transform(JavaAppStartScriptGenerationDetails javaAppStartScriptGenerationDetails) {
        Map<String, String> base = StartScriptTemplateBindingFactory.unix().transform(javaAppStartScriptGenerationDetails);

        base.put("classpath", this.classpath);

        if (log.isInfoEnabled()) {
            base.entrySet().stream().map(Objects::toString).forEach(log::info);
        }

        return base;
    }
}
