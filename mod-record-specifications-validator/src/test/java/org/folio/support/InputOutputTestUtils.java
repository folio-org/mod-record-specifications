package org.folio.support;

import java.io.File;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.marc4j.MarcJsonReader;
import org.marc4j.marc.Record;

@UtilityClass
public class InputOutputTestUtils {

  @SneakyThrows
  public Record readRecord(String filePath) {
    var file = getFile(filePath);
    var marcJsonReader = new MarcJsonReader(new FileReader(file));
    return marcJsonReader.next();
  }

  private static File getFile(String filename) throws URISyntaxException {
    return new File(Objects.requireNonNull(InputOutputTestUtils.class.getClassLoader().getResource(filename)).toURI());
  }
}
