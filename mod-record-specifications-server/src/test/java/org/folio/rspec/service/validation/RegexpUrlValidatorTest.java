package org.folio.rspec.service.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@UnitTest
class RegexpUrlValidatorTest {

  private final RegexpUrlValidator validator = new RegexpUrlValidator();

  @ValueSource(strings = {
    "http://www.google.com",
    "   http://www.google.com    ",
    "https://www.apple.com/shop/buy-iphone/iphone12",
    "http://edition.cnn.com",
    "https://www.example.com?query=hello&page=2",
    "http://www.test.com/path/to/page?name=test&num=123",
    "https://images.unsplash.com/photo-1465146633011-14f8e0781094",
    "https://255.255.255.255",
    "http://localhost:8080/testPage",
    "https://en.wikipedia.org/wiki/Main_Page",
    "http://www.example.co.uk",
    "https://www.amazon.com/Kindle-Store/b?ie=UTF8&node=133140011",
    "http://35.160.111.237/reports/pages/device.html",
    "https://sub.domain.example-site.com:8080/path/to/myfile.html?key1=value1&key2=value2#InTheDocument",
    "http://example.com:80/path/to/myfile.jpg",
    "https://example.edu",
    "http://test-site.io/path?query=value#fragment",
    "https://mysite123.me/info",
    "http://another-example.org/download/file"
  })
  @ParameterizedTest
  void isValid_ValidUrls(String url) {
    assertTrue(validator.isValid(url, null));
  }
}
