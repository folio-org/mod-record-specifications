package org.folio.rspec.validation.converter;

public interface Converter<S, T> {

  T convert(S source);
}
