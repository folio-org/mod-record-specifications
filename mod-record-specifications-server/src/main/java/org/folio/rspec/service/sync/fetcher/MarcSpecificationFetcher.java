package org.folio.rspec.service.sync.fetcher;

import com.fasterxml.jackson.databind.node.ArrayNode;
import java.nio.charset.StandardCharsets;
import lombok.extern.log4j.Log4j2;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.exception.SpecificationFetchingFailedException;
import org.folio.rspec.utils.FileUtils;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class MarcSpecificationFetcher {

  private static final String SPEC_PATH_TEMPLATE = "spec/%s/%s.html";

  private final MarcSpecificationParser parser;

  public MarcSpecificationFetcher(MarcSpecificationParser parser) {
    this.parser = parser;
  }

  public ArrayNode fetch(Family family, FamilyProfile profile) {
    try {
      var specPath = String.format(SPEC_PATH_TEMPLATE, family.getValue(), profile.getValue()).toLowerCase();
      var resource = FileUtils.getInputStream(specPath);
      return parser.parse(Jsoup.parse(resource, StandardCharsets.UTF_8.name(), ""));
    } catch (Exception e) {
      log.error("Failed to fetch specification for family={}, profile={}.", family, profile, e);
      throw new SpecificationFetchingFailedException(e);
    }
  }
}
