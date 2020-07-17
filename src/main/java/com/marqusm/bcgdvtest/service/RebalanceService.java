package com.marqusm.bcgdvtest.service;

import com.marqusm.bcgdvtest.model.Customer;
import com.marqusm.bcgdvtest.model.RebalansResponse;
import com.marqusm.bcgdvtest.model.Strategy;
import com.marqusm.bcgdvtest.model.external.fpsservice.FpsCustomer;
import com.marqusm.bcgdvtest.model.external.fpsservice.FpsCustomerAction;
import com.marqusm.bcgdvtest.service.base.FpsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class RebalanceService {

  private final CsvService csvService;
  private final FpsService fpsService;
  private final CustomerService customerService;
  private final ConfigurationService configurationService;

  public List<RebalansResponse> rebalance(MultipartFile customersFile, MultipartFile strategyFile) {
    val parsed = parseFiles(customersFile, strategyFile);
    val customers = parsed.getKey();
    val strategies = parsed.getValue();
    val actions = createActions(customers, strategies);
    return executeActions(actions);
  }

  protected AbstractMap.SimpleEntry<List<Customer>, List<Strategy>> parseFiles(
      MultipartFile customersFile, MultipartFile strategyFile) {
    List<Customer> customers;
    List<Strategy> strategies;
    try {
      customers = csvService.parseCustomers(customersFile.getInputStream());
      strategies = csvService.parseStrategy(strategyFile.getInputStream());
      return new AbstractMap.SimpleEntry<>(customers, strategies);
    } catch (IOException e) {
      throw new RuntimeException("Received files opening fails");
    }
  }

  protected List<FpsCustomerAction> createActions(
      List<Customer> customers, List<Strategy> strategies) {
    return customers.stream()
        .map(
            customer -> {
              val strategy = matchStrategy(customer, strategies);
              val fpsCustomer = fpsService.getCustomer(customer.getCustomerId());
              return createAction(fpsCustomer, strategy);
            })
        .collect(Collectors.toList());
  }

  protected List<RebalansResponse> executeActions(List<FpsCustomerAction> actions) {
    val currentActions = new ArrayList<>(actions);
    List<FpsCustomerAction> currentBatch;
    val rebalanseResponses = new LinkedList<RebalansResponse>();
    var lastProcessedIndex = 0;
    while (lastProcessedIndex < currentActions.size()) {
      currentBatch =
          currentActions.subList(
              lastProcessedIndex,
              Math.min(
                  lastProcessedIndex + configurationService.getActionsCountInBatch(),
                  currentActions.size()));
      val fpsExecuteResponse = fpsService.execute(currentBatch);
      lastProcessedIndex += configurationService.getActionsCountInBatch();
      val isSuccessful = fpsExecuteResponse.getStatusCode().equals(HttpStatus.CREATED);
      if (!isSuccessful) {
        log.warn("FPS Service call failed. Status: " + fpsExecuteResponse.getStatusCode());
      }
      rebalanseResponses.addAll(
          currentBatch.stream()
              .map((a) -> RebalansResponse.of(a.getCustomerId(), isSuccessful))
              .collect(Collectors.toList()));
    }
    return rebalanseResponses;
  }

  protected Strategy matchStrategy(Customer customer, List<Strategy> strategies) {
    return strategies.stream()
        .filter(s -> isMatching(customer, s))
        .findFirst()
        .orElse(Strategy.of(-1L, null, null, null, null, 0, 100, 0));
  }

  protected FpsCustomerAction createAction(FpsCustomer fpsCustomer, Strategy strategy) {
    val total = fpsCustomer.getStocks() + fpsCustomer.getBonds() + fpsCustomer.getCash();
    FpsCustomer goal = FpsCustomer.builder().customerId(fpsCustomer.getCustomerId()).build();
    goal.setStocks(Math.round(strategy.getStocksPercentage() * total / 100.f));
    goal.setBonds(Math.round(strategy.getBondsPercentage() * total / 100.f));
    goal.setCash(total - goal.getStocks() - goal.getBonds());
    val fpsAction =
        FpsCustomerAction.of(
            fpsCustomer.getCustomerId(),
            goal.getStocks() - fpsCustomer.getStocks(),
            goal.getBonds() - fpsCustomer.getBonds(),
            goal.getCash() - fpsCustomer.getCash());
    log.debug("Creating action");
    log.debug("Current balance: " + fpsCustomer);
    log.debug("Selected strategy: " + strategy);
    log.debug("Target balance: " + goal);
    log.debug("Created action: " + fpsAction);
    return fpsAction;
  }

  private boolean isMatching(Customer customer, Strategy strategy) {
    if (customer.getRiskLevel() < strategy.getMinRiskLevel()) return false;
    if (customer.getRiskLevel() > strategy.getMaxRiskLevel()) return false;

    val yearsToRetirement = customerService.yearsToRetirement(customer);

    if (yearsToRetirement < strategy.getMinYearsToRetirement()) return false;
    if (yearsToRetirement > strategy.getMaxYearsToRetirement()) return false;

    return true;
  }
}
