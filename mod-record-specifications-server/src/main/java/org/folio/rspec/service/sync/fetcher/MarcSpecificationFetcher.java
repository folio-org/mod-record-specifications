package org.folio.rspec.service.sync.fetcher;

import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.folio.rspec.exception.SpecificationFetchingFailedException;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MarcSpecificationFetcher {

  private final MarcSpecificationParser parser;

  public ArrayNode fetch(String url) {
    try {
      var document = Jsoup.connect(url).get();
      return parser.parse(document);
    } catch (IOException e) {
      throw new SpecificationFetchingFailedException(e);
    }
  }
}
