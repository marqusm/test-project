package com.marqusm.bcgdvtest.service;

import com.marqusm.bcgdvtest.model.Customer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
@AllArgsConstructor
@Service
public class CustomerService {

  public int yearsToRetirement(Customer customer) {
    return customer.getRetirementAge()
        - (int) ChronoUnit.YEARS.between(customer.getDateOfBirth(), LocalDate.now());
  }
}
