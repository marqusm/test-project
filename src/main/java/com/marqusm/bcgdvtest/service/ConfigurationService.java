package com.marqusm.bcgdvtest.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Getter
@Service
public class ConfigurationService {
  @Value("${bcgdvtest.actions-in-batch:5}")
  private Integer actionsCountInBatch;
}
