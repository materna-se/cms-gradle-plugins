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
public abstract class PrepareChromeDriver extends OkHttpTask {

  @Input
  public abstract Property<String> getVersion();

  @OutputDirectory
  public abstract DirectoryProperty getOutputDir();

  @TaskAction
  public void download() throws IOException {
    OkHttpClient client = getOkHttpClient();

    String version = getVersion().getOrElse("74.0.3729.6");

    Request request = new Request.Builder()
        .get()
        .url("https://chromedriver.storage.googleapis.com/" + version + "/chromedriver_linux64.zip")
        .build();

    Response response = client.newCall(request).execute();

    File tmpFile = new File(getTemporaryDir(), "chromedriver_linux64.zip");

    response.body().source().readAll(Okio.sink(tmpFile));

    getProject().sync(spec -> {
      spec.from(getProject().zipTree(tmpFile));
      spec.into(getOutputDir());
    });
  }
}
