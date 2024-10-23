/*
 * Copyright © 2020 Materna Information & Communications SE
 */

package de.materna.cms.gradle.plugins.test;

import io.freefair.gradle.plugins.okhttp.tasks.OkHttpTask;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Okio;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

@Getter
@CacheableTask
public abstract class PrepareGeckoDriver extends OkHttpTask {

  @Input
  public abstract Property<String> getVersion();

  @OutputDirectory
  public abstract DirectoryProperty getOutputDir();

  @TaskAction
  public void download() throws IOException {
    OkHttpClient client = new OkHttpClient();

    String version = getVersion().getOrElse("0.24.0");

    if (!version.startsWith("v")) {
      version = "v" + version;
    }

    String fileName = "geckodriver-" + version + "-linux64.tar.gz";
    Request request = new Request.Builder()
        .get()
        .url("https://github.com/mozilla/geckodriver/releases/download/" + version + "/" + fileName)
        .build();

    Response response = client.newCall(request).execute();

    File tmpFile = new File(getTemporaryDir(), fileName);

    response.body().source().readAll(Okio.sink(tmpFile));

    getProject().sync(spec -> {
      spec.from(getProject().tarTree(getProject().getResources().gzip(tmpFile)));
      spec.into(getOutputDir());
    });
  }
}
