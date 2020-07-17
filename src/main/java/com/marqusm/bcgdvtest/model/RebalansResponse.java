package com.marqusm.bcgdvtest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Data
public class RebalansResponse {
  private Long customerId;
  private Boolean isSuccessful;
}
