package com.farmtofolk.farmtofolk_ledger.publictrace;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicTraceController {

  private final PublicTraceService publicTraceService;

  public PublicTraceController(PublicTraceService publicTraceService) {
    this.publicTraceService = publicTraceService;
  }

  @GetMapping("/api/public/trace/{publicToken}")
  public PublicTraceResponse getPublicTrace(@PathVariable String publicToken) {
    return publicTraceService.getPublicTrace(publicToken);
  }
}
