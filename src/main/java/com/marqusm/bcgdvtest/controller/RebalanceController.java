package com.marqusm.bcgdvtest.controller;

import com.marqusm.bcgdvtest.model.RebalansResponse;
import com.marqusm.bcgdvtest.service.RebalanceService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/rebalance")
@RestController
public class RebalanceController {

  private final RebalanceService rebalanceService;

  @PostMapping
  public List<RebalansResponse> rebalance(
      @RequestParam("customers") MultipartFile customers,
      @RequestParam("strategy") MultipartFile strategy) {
    return rebalanceService.rebalance(customers, strategy);
  }
}
