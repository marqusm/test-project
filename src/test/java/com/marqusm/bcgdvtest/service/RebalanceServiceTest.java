package com.marqusm.bcgdvtest.service;

import com.marqusm.bcgdvtest.model.Customer;
import com.marqusm.bcgdvtest.model.RebalansResponse;
import com.marqusm.bcgdvtest.model.Strategy;
import com.marqusm.bcgdvtest.model.external.fpsservice.FpsCustomer;
import com.marqusm.bcgdvtest.model.external.fpsservice.FpsCustomerAction;
import com.marqusm.bcgdvtest.service.base.FpsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RebalanceServiceTest {

  private static final List<Strategy> STRATEGIES =
      List.of(
          Strategy.of(1L, 0, 3, 20, 30, 20, 20, 60),
          Strategy.of(2L, 0, 3, 10, 20, 10, 20, 70),
          Strategy.of(3L, 6, 9, 20, 30, 10, 0, 90));

  @InjectMocks RebalanceService rebalanceService;
  @Mock CustomerService customerService;
  @Mock FpsService fpsService;
  @Mock CsvService csvService;
  @Mock ConfigurationService configurationService;

  @Test
  void matchStrategy_01() {
    when(customerService.yearsToRetirement(any())).thenReturn(14);
    Customer customer = Customer.of(1L, "bob@bob.com", LocalDate.parse("1961-04-29"), 3, 65);

    Strategy strategy = rebalanceService.matchStrategy(customer, STRATEGIES);

    Assertions.assertEquals(2, strategy.getStrategyId());
  }

  @Test
  void matchStrategy_02() {
    when(customerService.yearsToRetirement(any())).thenReturn(33);
    Customer customer = Customer.of(2L, "sally@gmail.com", LocalDate.parse("1978-05-01"), 8, 67);

    Strategy strategy = rebalanceService.matchStrategy(customer, STRATEGIES);

    Assertions.assertEquals(-1, strategy.getStrategyId());
  }

  @Test
  void matchStrategy_03() {
    when(customerService.yearsToRetirement(any())).thenReturn(20);
    Customer customer = Customer.of(3L, "marry@gmail.com", LocalDate.parse("1972-03-15"), 6, 67);

    Strategy strategy = rebalanceService.matchStrategy(customer, STRATEGIES);

    Assertions.assertEquals(3, strategy.getStrategyId());
  }

  @Test
  void createAction() {
    FpsCustomer fpsCustomer = FpsCustomer.of(1L, 6700, 1200, 400);
    Strategy strategy = STRATEGIES.get(1);
    FpsCustomerAction expected = FpsCustomerAction.of(1L, -5870, 4610, 1260);

    FpsCustomerAction action = rebalanceService.createAction(fpsCustomer, strategy);

    Assertions.assertEquals(expected, action);
  }

  @Test
  void balance_twoBatches() throws IOException {
    when(fpsService.execute(any())).thenReturn(new ResponseEntity<>(HttpStatus.CREATED));
    when(csvService.parseCustomers(any()))
        .thenReturn(
            IntStream.range(0, 10)
                .boxed()
                .map(i -> Customer.of(1L, "bob@bob.com", LocalDate.parse("1961-04-29"), 3, 65))
                .collect(Collectors.toList()));
    when(csvService.parseStrategy(any()))
        .thenReturn(List.of(Strategy.of(2L, 0, 3, 10, 20, 10, 20, 70)));
    when(fpsService.getCustomer(anyLong())).thenReturn(FpsCustomer.of(1L, 20, 30, 50));
    when(configurationService.getActionsCountInBatch()).thenReturn(5);

    rebalanceService.rebalance(
        new MockMultipartFile(
            "customers.csv", new ClassPathResource("customers.csv").getInputStream()),
        new MockMultipartFile(
            "strategy.csv", new ClassPathResource("strategy.csv").getInputStream()));

    verify(fpsService, times(2)).execute(any());
  }

  @Test
  void balance_fpsServiceFailedStatusCode() throws IOException {
    when(fpsService.execute(any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    when(csvService.parseCustomers(any()))
        .thenReturn(List.of(Customer.of(1L, "bob@bob.com", LocalDate.parse("1961-04-29"), 3, 65)));
    when(csvService.parseStrategy(any()))
        .thenReturn(List.of(Strategy.of(2L, 0, 3, 10, 20, 10, 20, 70)));
    when(fpsService.getCustomer(anyLong())).thenReturn(FpsCustomer.of(1L, 20, 30, 50));
    when(configurationService.getActionsCountInBatch()).thenReturn(3);
    RebalansResponse expected = new RebalansResponse();
    expected.setCustomerId(1L);
    expected.setIsSuccessful(false);

    List<RebalansResponse> response =
        rebalanceService.rebalance(
            new MockMultipartFile(
                "customers.csv", new ClassPathResource("customers.csv").getInputStream()),
            new MockMultipartFile(
                "strategy.csv", new ClassPathResource("strategy.csv").getInputStream()));

    Assertions.assertEquals(expected, response.get(0));
  }

  @Test
  void parseFiles_csvServiceFails() throws IOException {
    MultipartFile customerMultipartFile = Mockito.mock(MultipartFile.class);
    MultipartFile strategyMultipartFile = Mockito.mock(MultipartFile.class);
    when(customerMultipartFile.getInputStream()).thenThrow(new IOException("IO Error"));

    Assertions.assertThrows(
        RuntimeException.class,
        () -> rebalanceService.parseFiles(customerMultipartFile, strategyMultipartFile));
  }
}
