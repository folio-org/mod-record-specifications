package org.folio.rspec.validation.validator.marc.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PRIVATE)
public final class Reference {

  private String tag;
  private Integer tagIndex;
  private Integer indicatorIndex;
  private Character subfield;
  private Integer subfieldIndex;

  public Reference(String tag) {
    this.tag = tag;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(tag);
    if (tagIndex != null) {
      sb.append("[").append(tagIndex).append("]");
    }
    if (indicatorIndex != null) {
      sb.append("^").append(indicatorIndex);
      return sb.toString();
    }
    if (subfield != null) {
      sb.append("$").append(subfield);
    }
    if (subfieldIndex != null) {
      sb.append("[").append(subfieldIndex).append("]");
    }
    return sb.toString();
  }

  public static Reference forTag(String tag, int tagIndex) {
    var reference = new Reference(tag);
    reference.setTagIndex(tagIndex);
    return reference;
  }

  public static Reference forTag(String tag) {
    return forTag(tag, 0);
  }

  public static Reference forIndicator(Reference fieldReference, int indicatorIndex) {
    var reference = forTag(fieldReference.getTag(), fieldReference.getTagIndex());
    reference.setIndicatorIndex(indicatorIndex);
    return reference;
  }

  public static Reference forSubfield(Reference fieldReference, Character subfield, int subfieldIndex) {
    var reference = forTag(fieldReference.getTag(), fieldReference.getTagIndex());
    reference.setSubfield(subfield);
    reference.setSubfieldIndex(subfieldIndex);
    return reference;
  }

  public static Reference forSubfield(Reference fieldReference, Character subfield) {
    return forSubfield(fieldReference, subfield, 0);
  }
}
