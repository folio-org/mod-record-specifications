package org.folio.rspec.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.rspec.config.JpaConfig.SYSTEM_USER_ID;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.AuditorAware;

@UnitTest
@ExtendWith(MockitoExtension.class)
class JpaConfigTest {

  @Mock
  private FolioExecutionContext folioExecutionContext;

  @InjectMocks
  private JpaConfig jpaConfig;

  @Test
  void testAuditorAware_whenThereIsUserInContext() {
    UUID randomUserId = UUID.randomUUID();
    when(folioExecutionContext.getUserId()).thenReturn(randomUserId);

    AuditorAware<UUID> auditorAware = jpaConfig.auditorProvider(folioExecutionContext);

    assertNotNull(auditorAware);
    assertThat(auditorAware.getCurrentAuditor()).hasValue(randomUserId);
  }

  @Test
  void testAuditorAware_whenThereIsNoUserInContext() {
    when(folioExecutionContext.getUserId()).thenReturn(null);

    AuditorAware<UUID> auditorAware = jpaConfig.auditorProvider(folioExecutionContext);

    assertNotNull(auditorAware);
    assertThat(auditorAware.getCurrentAuditor()).hasValue(SYSTEM_USER_ID);
  }
}
