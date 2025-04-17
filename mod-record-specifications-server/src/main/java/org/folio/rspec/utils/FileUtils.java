package org.folio.rspec.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

@Log4j2
@UtilityClass
public class FileUtils {

  private static final ClassLoader CLASSLOADER = FileUtils.class.getClassLoader();

  public static @NotNull InputStream getInputStream(String filename) {
    if (filename == null) {
      throw new IllegalArgumentException("Filename must not be null");
    }
    var resource = CLASSLOADER.getResourceAsStream(filename);
    if (resource == null) {
      throw new IllegalStateException(String.format("Resource not found for filename = '%s'", filename));
    }
    return resource;
  }

  public static String readString(String filename) {
    try {
      return IOUtils.toString(getInputStream(filename), UTF_8);
    } catch (IOException | IllegalStateException e) {
      log.warn("Failed to read resource file '{}'", filename, e);
      return null;
    }
  }
}
