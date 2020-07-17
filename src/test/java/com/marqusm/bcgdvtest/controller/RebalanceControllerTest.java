package com.marqusm.bcgdvtest.controller;

import com.marqusm.bcgdvtest.model.Customer;
import com.marqusm.bcgdvtest.model.external.fpsservice.FpsCustomer;
import com.marqusm.bcgdvtest.service.CustomerService;
import com.marqusm.bcgdvtest.service.MockFpsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RebalanceControllerTest {

  @Autowired MockMvc mockMvc;
  @Autowired MockFpsService mockFpsService;
  @MockBean CustomerService customerService;

  @Test
  void rebalance() throws Exception {
    Map<Long, Integer> yearsToRetirementMap = Map.of(1L, 14, 2L, 33, 3L, 20);
    when(customerService.yearsToRetirement(any()))
        .thenAnswer(
            invocation -> {
              Long customerId = invocation.getArgument(0, Customer.class).getCustomerId();
              return yearsToRetirementMap.get(customerId);
            });

    MockMultipartFile customers =
        new MockMultipartFile(
            "customers",
            "customers.csv",
            "text/csv",
            new ClassPathResource("customers.csv").getInputStream());
    MockMultipartFile strategy =
        new MockMultipartFile(
            "strategy",
            "strategy.csv",
            "text/csv",
            new ClassPathResource("strategy.csv").getInputStream());

    mockMvc
        .perform(multipart("/rebalance").file(customers).file(strategy))
        .andExpect(status().isOk());

    FpsCustomer customer1 = mockFpsService.getCustomer(1L);

    Assertions.assertEquals(300, customer1.getStocks());
    Assertions.assertEquals(2100, customer1.getBonds());
    Assertions.assertEquals(600, customer1.getCash());
  }

  @Test
  void rebalance_illegalCustomersFile() throws Exception {
    MockMultipartFile customers =
        new MockMultipartFile(
            "customers",
            "customers.csv",
            "text/csv",
            new ClassPathResource("customers_illegal.csv").getInputStream());
    MockMultipartFile strategy =
        new MockMultipartFile(
            "strategy",
            "strategy.csv",
            "text/csv",
            new ClassPathResource("strategy.csv").getInputStream());

    mockMvc
        .perform(multipart("/rebalance").file(customers).file(strategy))
        .andExpect(status().isBadRequest());
  }

  @Test
  void rebalance_illegalStrategyFile() throws Exception {
    MockMultipartFile customers =
        new MockMultipartFile(
            "customers",
            "customers.csv",
            "text/csv",
            new ClassPathResource("customers.csv").getInputStream());
    MockMultipartFile strategy =
        new MockMultipartFile(
            "strategy",
            "strategy.csv",
            "text/csv",
            new ClassPathResource("strategy_illegal.csv").getInputStream());

    mockMvc
        .perform(multipart("/rebalance").file(customers).file(strategy))
        .andExpect(status().isBadRequest());
  }

  @Test
  void rebalance_internalError() throws Exception {
    when(customerService.yearsToRetirement(any()))
        .thenThrow(new RuntimeException("Internal error"));

    MockMultipartFile customers =
        new MockMultipartFile(
            "customers",
            "customers.csv",
            "text/csv",
            new ClassPathResource("customers.csv").getInputStream());
    MockMultipartFile strategy =
        new MockMultipartFile(
            "strategy",
            "strategy.csv",
            "text/csv",
            new ClassPathResource("strategy.csv").getInputStream());

    mockMvc
        .perform(multipart("/rebalance").file(customers).file(strategy))
        .andExpect(status().isInternalServerError());
  }
}
