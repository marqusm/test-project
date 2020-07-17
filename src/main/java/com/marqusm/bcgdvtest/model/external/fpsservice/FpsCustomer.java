package com.marqusm.bcgdvtest.model.external.fpsservice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor(staticName = "of")
@Builder
@Data
public class FpsCustomer {
  private Long customerId;
  private Integer stocks;
  private Integer bonds;
  private Integer cash;
}
