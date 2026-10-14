package org.folio.rspec.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class AppliedSpecUpdateIdTest {

  private final AppliedSpecUpdateId id = new AppliedSpecUpdateId("CODE-1", Family.MARC, FamilyProfile.AUTHORITY);

  @Test
  void equalsAndHashCode_areBasedOnAllThreeKeyParts() {
    var same = new AppliedSpecUpdateId("CODE-1", Family.MARC, FamilyProfile.AUTHORITY);

    assertThat(id).isEqualTo(id).isEqualTo(same).hasSameHashCodeAs(same);
  }

  @Test
  void equals_isFalse_whenAnyKeyPartDiffers() {
    assertThat(id)
      .isNotEqualTo(new AppliedSpecUpdateId("CODE-2", Family.MARC, FamilyProfile.AUTHORITY))
      .isNotEqualTo(new AppliedSpecUpdateId("CODE-1", Family.MARC, FamilyProfile.BIBLIOGRAPHIC))
      .isNotEqualTo(new AppliedSpecUpdateId("CODE-1", null, FamilyProfile.AUTHORITY));
  }

  @Test
  void equals_isFalse_forNullAndForOtherTypes() {
    assertThat(id).isNotEqualTo(null).isNotEqualTo("CODE-1");
  }

  @Test
  void gettersAndToString_exposeTheKeyParts() {
    assertThat(id.getCode()).isEqualTo("CODE-1");
    assertThat(id.getFamily()).isEqualTo(Family.MARC);
    assertThat(id.getProfile()).isEqualTo(FamilyProfile.AUTHORITY);
    assertThat(id).hasToString("code=CODE-1, family=MARC, profile=authority");
  }
}
