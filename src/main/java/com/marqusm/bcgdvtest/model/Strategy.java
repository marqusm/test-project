package com.marqusm.bcgdvtest.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor(staticName = "of")
@Data
public class Strategy {
  private Long strategyId;
  private Integer minRiskLevel;
  private Integer maxRiskLevel;
  private Integer minYearsToRetirement;
  private Integer maxYearsToRetirement;
  private Integer stocksPercentage;
  private Integer cashPercentage;
  private Integer bondsPercentage;
}
