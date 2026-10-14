package org.folio.rspec.service.tenant;

import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;

/**
 * One entry in {@link MarcSpecUpdateService#KNOWN_UPDATES}: a ticket that fixed the bundled spec
 * HTML for the given {@code family}, scoped to a single {@code profile} (e.g. only
 * {@code FamilyProfile.BIBLIOGRAPHIC}) when the fix only ever touched that one, or to every
 * profile in the family when {@code profile} is {@code null}.
 */
public record SpecUpdate(String code, Family family, FamilyProfile profile) {
}
