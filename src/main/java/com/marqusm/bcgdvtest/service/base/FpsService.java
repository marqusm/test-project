package com.marqusm.bcgdvtest.service.base;

import com.marqusm.bcgdvtest.model.external.fpsservice.FpsCustomer;
import com.marqusm.bcgdvtest.model.external.fpsservice.FpsCustomerAction;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient("FpsService")
@Service
public interface FpsService {
  @GetMapping("/customer/{customerId}")
  FpsCustomer getCustomer(@PathVariable("customerId") Long customerId);

  @PostMapping(value = "/execute", consumes = "application/json")
  ResponseEntity<Void> execute(List<FpsCustomerAction> customerActions);
}
