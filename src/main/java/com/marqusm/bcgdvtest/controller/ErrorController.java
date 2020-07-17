package com.marqusm.bcgdvtest.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@AllArgsConstructor
@ControllerAdvice
public class ErrorController {

  @ExceptionHandler(IllegalArgumentException.class)
  public void processIllegalArgumentException(
      IllegalArgumentException ex, HttpServletResponse response) throws IOException {
    log.error("processRuntimeException", ex);
    response.sendError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
  }

  @ExceptionHandler(RuntimeException.class)
  public void processRuntimeException(RuntimeException ex, HttpServletResponse response)
      throws IOException {
    log.error("processRuntimeException", ex);
    response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
  }
}
