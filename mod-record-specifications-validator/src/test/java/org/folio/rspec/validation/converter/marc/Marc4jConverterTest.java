package org.folio.rspec.validation.converter.marc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;
import java.util.stream.Collectors;
import org.folio.rspec.validation.validator.marc.model.MarcControlField;
import org.folio.rspec.validation.validator.marc.model.MarcDataField;
import org.folio.rspec.validation.validator.marc.model.MarcRecord;
import org.folio.spring.testing.type.UnitTest;
import org.folio.support.TestRecordProvider;
import org.junit.jupiter.api.Test;
import org.marc4j.marc.Record;

@UnitTest
class Marc4jConverterTest {

  private final Marc4jConverter marc4jConverter = new Marc4jConverter();
  private final Record marc4jRecord = TestRecordProvider.getMarc4jRecord("testdata/marc-bib-record.json");

  @Test
  void testConvert_WhenCalled_ShouldReturnMarcRecord() {
    MarcRecord marcRecord = marc4jConverter.convert(marc4jRecord);
    assertEquals(6, marcRecord.getControlFields().size());
    assertEquals(9, marcRecord.getDataFields().size());

    assertThat(marcRecord.getControlFields())
      .hasSize(6)
      .extracting(controlField -> controlField.reference().toString(), MarcControlField::value)
      .containsExactlyInAnyOrder(
        tuple("000[0]", "01750ccm a2200421   4500"),
        tuple("001[0]", "393893"),
        tuple("005[0]", "20141107001016.0"),
        tuple("006[0]", "c bcdefghijklmn o "),
        tuple("007[0]", "sa bcdefghijkl"),
        tuple("008[0]", "abcdefghijklmnopqr bcdefghijklmn o stuvw")
      );

    assertThat(marcRecord.getDataFields())
      .hasSize(9)
      .extracting(dataField -> dataField.reference().toString(), controlFieldExtractor(), dataFieldExtractor()
      )
      .containsExactlyInAnyOrder(
        tuple("035[0]", "035[0]^1=#;035[0]^2=#", "035[0]$a[0]=(OCoLC)63611770"),
        tuple("650[0]", "650[0]^1=#;650[0]^2=0", "650[0]$a[0]=Instrumental music"),
        tuple("650[1]", "650[1]^1=#;650[1]^2=7",
          "650[1]$0[0]=(OCoLC)fst00974414 650[1]$a[0]=Instrumental music 650[1]$2[0]=fast"),
        tuple("650[2]", "650[2]^1=#;650[2]^2=7",
          "650[2]$0[0]=(OCoLC)fst01168379 650[2]$a[0]=Vocal music 650[2]$2[0]=fast"),
        tuple("100[0]", "100[0]^1=0;100[0]^2=#",
          "100[0]$0[0]=12345 100[0]$a[0]=Mozart, Wolfgang Amadeus, "
          + "100[0]$d[0]=1756-1791. 100[0]$9[0]=b9a5f035-de63-4e2c-92c2-07240c88b817"),
        tuple("035[1]", "035[1]^1=#;035[1]^2=#", "035[1]$a[0]=393893"),
        tuple("245[0]", "245[0]^1=1;245[0]^2=0",
          "245[0]$a[0]=Neue Ausgabe samtlicher Werke, "
          + "245[0]$b[0]=in Verbindung mit den Mozartstadten, Augsburg, Salzburg und Wien. "
          + "245[0]$c[0]=Hrsg. von der Internationalen Stiftung Mozarteum, Salzburg."),
        tuple("047[0]", "047[0]^1=#;047[0]^2=#",
          "047[0]$a[0]=cn 047[0]$a[1]=ct 047[0]$a[2]=co 047[0]$a[3]=df 047[0]$a[4]=dv "
          + "047[0]$a[5]=ft 047[0]$a[6]=fg 047[0]$a[7]=ms 047[0]$a[8]=mi 047[0]$a[9]=nc "
          + "047[0]$a[10]=op 047[0]$a[11]=ov 047[0]$a[12]=rq 047[0]$a[13]=sn 047[0]$a[14]=su "
          + "047[0]$a[15]=sy 047[0]$a[16]=vr 047[0]$a[17]=zz"),
        tuple("010[0]", "010[0]^1=#;010[0]^2=#", "010[0]$a[0]=  2001000234 010[0]$z[0]=0001 010[0]$z[1]=0002")
      );
  }

  private Function<MarcDataField, Object> dataFieldExtractor() {
    return dataField -> dataField.subfields().stream()
      .map(marcSubfield -> marcSubfield.reference().toString() + "=" + marcSubfield.value())
      .collect(Collectors.joining(" "));
  }

  private Function<MarcDataField, Object> controlFieldExtractor() {
    return dataField -> dataField.indicators().stream()
      .map(marcIndicator -> marcIndicator.reference().toString() + "=" + marcIndicator.value()).collect(
        Collectors.joining(";"));
  }
}
