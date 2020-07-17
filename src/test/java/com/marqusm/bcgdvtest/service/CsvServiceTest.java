package com.marqusm.bcgdvtest.service;

import com.marqusm.bcgdvtest.model.Customer;
import com.marqusm.bcgdvtest.model.Strategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class CsvServiceTest {

  @InjectMocks CsvService csvService;

  @Test
  void fetchCustomer() {
    String text = "1,bob@bob.com,1961-04-29,3,65";
    Customer expected = Customer.of(1L, "bob@bob.com", LocalDate.of(1961, 4, 29), 3, 65);

    Customer customer = csvService.fetchCustomer(text, 0);

    Assertions.assertEquals(expected, customer);
  }

  @Test
  void fetchStrategy() {
    String text = "1,0,3,20,30,20,20,60";
    Strategy expected = Strategy.of(1L, 0, 3, 20, 30, 20, 20, 60);

    Strategy strategy = csvService.fetchStrategy(text, 0);

    Assertions.assertEquals(expected, strategy);
  }

  @Test
  void parseInputStream() {
    InputStream inputStream = Mockito.mock(InputStream.class);

    Assertions.assertThrows(
        RuntimeException.class, () -> csvService.parseInputStream(inputStream, null));
  }
}
