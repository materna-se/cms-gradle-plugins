package de.bund.gsb.gradle.plugins.component;

import lombok.experimental.UtilityClass;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.jvm.application.tasks.CreateStartScripts;

@UtilityClass
public class StartScriptUtil {

    public static void disableWindowsScript(CreateStartScripts task) {
        task.setWindowsStartScriptGenerator((javaAppStartScriptGenerationDetails, writer) -> {});
        //noinspection Convert2Lambda
        task.doLast(new Action<>() {
            @Override
            public void execute(Task t) {
                t.getProject().delete(task.getWindowsScript());
            }
        });
    }


}
