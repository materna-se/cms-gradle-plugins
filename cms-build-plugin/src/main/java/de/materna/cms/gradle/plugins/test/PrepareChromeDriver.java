/*
 * Copyright 2025 Materna Information & Communications SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
@Deprecated
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
