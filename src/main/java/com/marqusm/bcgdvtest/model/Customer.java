package com.marqusm.bcgdvtest.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@AllArgsConstructor(staticName = "of")
@Data
public class Customer {
  private Long customerId;
  private String email;
  private LocalDate dateOfBirth;
  private int riskLevel;
  private int retirementAge;
}
