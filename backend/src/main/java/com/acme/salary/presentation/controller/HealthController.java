package com.acme.salary.presentation.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class HealthController {
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {return ResponseEntity.ok(Map.of("status", "UP"));}
}
