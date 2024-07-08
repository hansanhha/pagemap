package com.bintage.pagemap.global;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/pagemap/global/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("good");
    }
}
