package org.folio.rspec.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
public class TranslationProviderDelegateTest {

  @Mock
  private TranslationProvider delegate;

  @Test
  public void format_appliesKeyPrefixAndDelegates() {
    TranslationProviderDelegate delegateUnderTest = new TranslationProviderDelegate(delegate);

    String key = "key";
    String formattedKey = "mod-record-specifications.validation.key";
    Object[] args = new Object[] {"arg1", "arg2"};
    when(delegate.format(formattedKey, args)).thenReturn(formattedKey);

    String result = delegateUnderTest.format(key, args);

    assertEquals(formattedKey, result);
    verify(delegate, times(1)).format(formattedKey, args);
  }

}
