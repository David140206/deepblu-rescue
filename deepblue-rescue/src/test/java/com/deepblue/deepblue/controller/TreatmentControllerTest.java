package com.deepblue.deepblue.controller;

import com.deepblue.deepblue.exception.GlobalExceptionHandler;
import com.deepblue.deepblue.service.TreatmentService;
import com.deepblue.deepblue.dto.request.CreateTreatmentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TreatmentController.class)
@Import(GlobalExceptionHandler.class)
class TreatmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TreatmentService service;

    @Test
    void shouldCreateTreatmentSuccessfully() throws Exception {
        String jsonPayload = """
                {
                    "animalCode": "AN-001",
                    "specialistCode": "SPEC-001",
                    "performedAt": "2026-08-21T09:00:00",
                    "type": "WOUND_CARE",
                    "description": "Cleaning and treatment of flipper injury."
                }
                """;

        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated());

        verify(service).register(any(CreateTreatmentRequest.class));
    }

    @Test
    void shouldReturn400WhenInvalidTreatmentRequest() throws Exception {
        String invalidJsonPayload = """
                {
                    "animalCode": "",
                    "specialistCode": "",
                    "type": null,
                    "description": ""
                }
                """;

        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJsonPayload))
                .andExpect(status().isBadRequest());

        verify(service, never()).register(any());
    }
    @Test
    void shouldReturn409WhenBusinessRuleViolated() throws Exception {
        String jsonPayload = """
                {
                    "animalCode": "AN-001",
                    "specialistCode": "SPEC-001",
                    "performedAt": "2026-08-21T09:00:00",
                    "type": "WOUND_CARE",
                    "description": "Cleaning and treatment of flipper injury."
                }
                """;

        // Configurar el servicio para que lance una excepción de regla de negocio
        when(service.register(any())).thenThrow(
                new com.deepblue.deepblue.exception.BusinessRuleException("Released animals cannot receive treatments")
        );

        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict()) // Valida código 409
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Released animals cannot receive treatments"))
                .andExpect(jsonPath("$.details").isMap());
    }
    @Test
    void shouldReturn400WithDetailsWhenInvalidTreatmentRequest() throws Exception {
        String invalidJsonPayload = """
                {
                    "animalCode": "",
                    "specialistCode": "",
                    "type": null,
                    "description": ""
                }
                """;

        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJsonPayload))
                .andExpect(status().isBadRequest()) // Valida código 400
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.details").isMap())
                .andExpect(jsonPath("$.details.animalCode").exists()) // Valida que detalle el campo específico
                .andExpect(jsonPath("$.details.specialistCode").exists());

        // Verifica con Mockito que el servicio NUNCA fue llamado debido a que falló el DTO
        verify(service, never()).register(any());
    }
}