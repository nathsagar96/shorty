package com.shorty.urls;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RedirectionController {

  private final UrlService urlService;

  public RedirectionController(UrlService urlService) {
    this.urlService = urlService;
  }

  @GetMapping("/{shortCode}")
  public void redirect(@PathVariable String shortCode, HttpServletResponse response)
      throws IOException {
    Optional<Url> url = urlService.findByShortCode(shortCode);

    if (url.isPresent()) {
      response.sendRedirect(url.get().originalUrl());
    } else {
      response.sendError(HttpStatus.NOT_FOUND.value(), "URL not found");
    }
  }
}
