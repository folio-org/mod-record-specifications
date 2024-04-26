package org.folio.support;

import java.util.ArrayList;
import org.apache.commons.lang3.tuple.Pair;

public class QueryParams extends ArrayList<Pair<String, String>> {

  public QueryParams addQueryParam(String key, String value) {
    add(Pair.of(key, value));
    return this;
  }
}
