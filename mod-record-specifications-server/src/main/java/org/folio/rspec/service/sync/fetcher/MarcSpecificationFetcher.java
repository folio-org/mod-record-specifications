package org.folio.rspec.service.sync.fetcher;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.log4j.Log4j2;
import org.folio.rspec.exception.SpecificationFetchingFailedException;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class MarcSpecificationFetcher {

  private final MarcSpecificationParser parser;
  private final RetryTemplate retryTemplate;

  public MarcSpecificationFetcher(MarcSpecificationParser parser,
                                  @Qualifier("marcSpecificationFetcherRetryTemplate") RetryTemplate retryTemplate) {
    this.parser = parser;
    this.retryTemplate = retryTemplate;
  }

  public ArrayNode fetch(String url) {
    return retryTemplate.execute(context -> {
      log.info("Fetching Marc Specification from {}, retry #{}", url, context.getRetryCount());
      try {
        var document = Jsoup.connect(url).timeout(30000).get();
        return parser.parse(document);
      } catch (Exception e) {
        log.error("Failed to fetch url.", e);
        throw new SpecificationFetchingFailedException(e);
      }
    });
  }
}
