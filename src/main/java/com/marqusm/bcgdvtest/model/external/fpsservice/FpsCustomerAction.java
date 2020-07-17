package com.marqusm.bcgdvtest.model.external.fpsservice;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor(staticName = "of")
@Data
public class FpsCustomerAction {
  private Long customerId;
  private Integer stocks;
  private Integer bonds;
  private Integer cash;
}
