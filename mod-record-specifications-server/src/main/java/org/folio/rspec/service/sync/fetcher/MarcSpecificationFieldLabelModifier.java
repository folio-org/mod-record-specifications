package org.folio.rspec.service.sync.fetcher;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.text.WordUtils.capitalize;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MarcSpecificationFieldLabelModifier {

  private static final Set<String> LOWERCASE_EXCLUSIONS = Set.of("of", "in", "and", "or", "to", "for", "the", "an");
  private static final Set<String> UPPERCASE_EXCLUSIONS = Set.of("ISSN", "LC", "GPO");

  private static final Pattern WORD_SPLIT_PATTERN = Pattern.compile("\\s+");
  private static final char[] WORD_DELIMITERS = {' ', '/'};

  private static final String HYPHEN_PATTERN = "--";
  private static final String REPLACEMENT = " - ";

  /**
   * Modify and clean up the given input string.
   *
   * @param input the string to modify
   * @return the modified string
   */
  public String modify(String input) {
    String cleanedInput = cleanUpInput(input);
    return capitalizeWords(cleanedInput);
  }

  /**
   * Capitalize the appropriate words in a given string.
   *
   * @param input the string to capitalize
   * @return the capitalized string
   */
  private String capitalizeWords(String input) {
    return Arrays.stream(WORD_SPLIT_PATTERN.split(input))
      .map(this::capitalizeWordIfNeeded)
      .collect(Collectors.joining(SPACE));
  }

  /**
   * Based on {@link MarcSpecificationFieldLabelModifier#LOWERCASE_EXCLUSIONS}
   * and {@link MarcSpecificationFieldLabelModifier#UPPERCASE_EXCLUSIONS} capitalize or don't the word.
   *
   * @param word the word to capitalize
   * @return the capitalized (or unchanged) word
   */
  private String capitalizeWordIfNeeded(String word) {
    if (UPPERCASE_EXCLUSIONS.contains(word)) {
      return word;
    }

    word = word.toLowerCase();

    if (!LOWERCASE_EXCLUSIONS.contains(word)) {
      return capitalize(word, WORD_DELIMITERS);
    } else {
      return word;
    }
  }

  /**
   * Clean up a given string, replacing all instances of a double hyphen with a hyphen surrounded by spaces.
   *
   * @param input the string to clean up
   * @return the cleaned-up string
   */
  private String cleanUpInput(String input) {
    return input.replaceAll(HYPHEN_PATTERN, REPLACEMENT).trim();
  }
}
