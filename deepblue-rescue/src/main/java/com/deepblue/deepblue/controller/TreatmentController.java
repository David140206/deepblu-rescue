package com.deepblue.deepblue.controller;

import com.deepblue.deepblue.dto.request.CreateTreatmentRequest;
import com.deepblue.deepblue.dto.response.TreatmentResponse;
import com.deepblue.deepblue.service.TreatmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/treatments")
public class TreatmentController {

    private final TreatmentService service;

    public TreatmentController(
            TreatmentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TreatmentResponse>
    register(
            @Valid
            @RequestBody
            CreateTreatmentRequest request) {
        TreatmentResponse response =
                service.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{animalCode}/treatments")
    public ResponseEntity<List<TreatmentResponse>>
    findTreatments(
            @PathVariable String animalCode) {
        return ResponseEntity.ok(
                service.findByAnimalCode(animalCode)
        );
    }
}
