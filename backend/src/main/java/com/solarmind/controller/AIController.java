package com.solarmind.controller;

import com.solarmind.dto.request.AskRequest;
import com.solarmind.dto.response.AskResponse;
import com.solarmind.service.AIService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {
  private final AIService s;

  public AIController(AIService s) {
    this.s = s;
  }

  @PostMapping("/ask")
  AskResponse ask(@Valid @RequestBody AskRequest r) {
    return s.ask(r);
  }
}
