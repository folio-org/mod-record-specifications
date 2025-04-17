package org.folio.rspec.utils;

import static org.folio.rspec.utils.FileUtils.getInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class FileUtilsTest {

  @Test
  void testGetInputStream_ValidFile() {
    var inputStream = getInputStream("test-resources/test-file.txt");
    assertNotNull(inputStream, "InputStream should not be null for a valid file");
  }

  @Test
  void testGetInputStream_InvalidFile() {
    var exception = assertThrows(IllegalStateException.class, () ->
      getInputStream("non-existent-file.txt"));
    assertEquals("Resource not found for filename = 'non-existent-file.txt'", exception.getMessage());
  }

  @Test
  void testGetInputStream_NullFile() {
    var exception = assertThrows(IllegalArgumentException.class, () ->
      getInputStream(null));
    assertEquals("Filename must not be null", exception.getMessage());
  }

  @Test
  void testReadString_ValidFile() {
    var content = FileUtils.readString("test-resources/test-file.txt");
    assertNotNull(content, "Content should not be null for a valid file");
    assertEquals("test-data", content.trim(), "Content should match the file content");
  }

  @Test
  void testReadString_InvalidFile() {
    var content = FileUtils.readString("non-existent-file.txt");
    assertNull(content, "Content should be null for a non-existent file");
  }
}
