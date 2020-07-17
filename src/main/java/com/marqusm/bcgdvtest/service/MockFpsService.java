package com.marqusm.bcgdvtest.service;

import com.marqusm.bcgdvtest.model.external.fpsservice.FpsCustomer;
import com.marqusm.bcgdvtest.model.external.fpsservice.FpsCustomerAction;
import com.marqusm.bcgdvtest.service.base.FpsService;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MockFpsService implements FpsService {

  final Map<Long, FpsCustomer> customerMap;

  public MockFpsService() {
    customerMap = new HashMap<>();
  }

  @Override
  public FpsCustomer getCustomer(Long customerId) {
    return customerMap.computeIfAbsent(customerId, id -> FpsCustomer.of(id, 1000, 1000, 1000));
  }

  @Override
  public ResponseEntity<Void> execute(List<FpsCustomerAction> customerActions) {
    customerActions.forEach(
        customerAction -> {
          val customer = getCustomer(customerAction.getCustomerId());
          customer.setStocks(customer.getStocks() + customerAction.getStocks());
          customer.setBonds(customer.getBonds() + customerAction.getBonds());
          customer.setCash(customer.getCash() + customerAction.getCash());
        });
    return new ResponseEntity<>(HttpStatus.CREATED);
  }
}
