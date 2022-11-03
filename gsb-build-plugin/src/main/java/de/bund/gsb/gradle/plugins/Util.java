package de.bund.gsb.gradle.plugins;

import lombok.experimental.UtilityClass;
import org.gradle.api.file.CopySpec;

@UtilityClass
public class Util {

    public static void stripFirstPathSegment(CopySpec copySpec) {
        copySpec.eachFile(fileCopyDetails -> {
            String oldSourcePath = fileCopyDetails.getSourcePath();
            String newSourcePath = oldSourcePath.substring(oldSourcePath.indexOf("/"));

            String path = fileCopyDetails.getPath();
            fileCopyDetails.setPath(path.replace(oldSourcePath, newSourcePath));
        });
        copySpec.setIncludeEmptyDirs(false);
    }
}
