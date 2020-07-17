package com.marqusm.bcgdvtest.service;

import com.marqusm.bcgdvtest.model.Customer;
import com.marqusm.bcgdvtest.model.Strategy;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

@Slf4j
@Service
public class CsvService {

  private final static String CSV_DELIMITER = ",";

  public List<Customer> parseCustomers(InputStream inputStream) {
    return parseInputStream(inputStream, this::fetchCustomer);
  }

  public List<Strategy> parseStrategy(InputStream inputStream) {
    return parseInputStream(inputStream, this::fetchStrategy);
  }

  protected <R> List<R> parseInputStream(
      InputStream inputStream, BiFunction<String, Integer, R> processLine) {
    val items = new LinkedList<R>();
    try (BufferedReader csvReader = new BufferedReader(new InputStreamReader(inputStream))) {
      String row;
      int rowIndex = 1;
      csvReader.readLine();
      while ((row = csvReader.readLine()) != null) {
        val item = processLine.apply(row, rowIndex++);
        items.add(item);
      }
      return items;
    } catch (IOException e) {
      throw new RuntimeException("Input stream opening error");
    }
  }

  protected Customer fetchCustomer(String textLine, int rowIndex) {
    String[] data = textLine.split(CSV_DELIMITER);
    checkCustomerRawData(data, rowIndex);
    return Customer.of(
        Long.parseLong(data[0]),
        data[1],
        LocalDate.parse(data[2]),
        Integer.parseInt(data[3]),
        Integer.parseInt(data[4]));
  }

  protected Strategy fetchStrategy(String textLine, int rowIndex) {
    String[] data = textLine.split(CSV_DELIMITER);
    checkStrategyRawData(data, rowIndex);
    return Strategy.of(
        Long.parseLong(data[0]),
        Integer.parseInt(data[1]),
        Integer.parseInt(data[2]),
        Integer.parseInt(data[3]),
        Integer.parseInt(data[4]),
        Integer.parseInt(data[5]),
        Integer.parseInt(data[6]),
        Integer.parseInt(data[7]));
  }

  private void checkCustomerRawData(String[] data, int rowIndex) {
    if (data.length != 5) {
      throw new IllegalArgumentException(
          "Illegal data size: " + data.length + ". Row: " + rowIndex);
    }
  }

  private void checkStrategyRawData(String[] data, int rowIndex) {
    if (data.length != 8) {
      throw new IllegalArgumentException(
          "Illegal data size: " + data.length + ". Row: " + rowIndex);
    }
  }
}
