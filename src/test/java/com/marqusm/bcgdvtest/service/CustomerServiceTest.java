package com.marqusm.bcgdvtest.service;

import com.marqusm.bcgdvtest.model.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

class CustomerServiceTest {

  private final CustomerService customerService = new CustomerService();

  @Test
  void yearsToRetirement() {
    Customer customer = Customer.of(1L, "bob@bob.com", LocalDate.of(1961, 4, 29), 3, 65);
    int expected =
        customer.getRetirementAge()
            - (int) ChronoUnit.YEARS.between(customer.getDateOfBirth(), LocalDate.now());

    int years = customerService.yearsToRetirement(customer);

    Assertions.assertEquals(expected, years);
  }
}
