package com.acme.salary.presentation.controller;

import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Health")
public class HealthController {
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {return ResponseEntity.ok(Map.of("status", "UP"));}
}
