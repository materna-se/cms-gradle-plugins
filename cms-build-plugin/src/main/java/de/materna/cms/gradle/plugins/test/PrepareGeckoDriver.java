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
import org.gradle.api.file.ArchiveOperations;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

@Getter
@CacheableTask
public abstract class PrepareGeckoDriver extends OkHttpTask {

  @Input
  public abstract Property<String> getVersion();

  @OutputDirectory
  public abstract DirectoryProperty getOutputDir();

  @Inject
  public abstract FileSystemOperations getFileSystemOperations();

  @Inject
  public abstract ArchiveOperations getArchiveOperations();

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

    getFileSystemOperations().sync(spec -> {
      spec.from(getArchiveOperations().tarTree(getArchiveOperations().gzip(tmpFile)));
      spec.into(getOutputDir());
    });
  }
}
