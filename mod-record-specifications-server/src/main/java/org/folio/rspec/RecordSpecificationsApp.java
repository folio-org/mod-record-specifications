package org.folio.rspec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.folio.rspec")
public class RecordSpecificationsApp {

  public static void main(String[] args) {
    SpringApplication.run(RecordSpecificationsApp.class, args);
  }

}
