package com.deepblue.deepblue.controller;

import com.deepblue.deepblue.domain.RescueStatus;
import com.deepblue.deepblue.dto.response.RescueCaseResponse;
import com.deepblue.deepblue.exception.BusinessRuleException;
import com.deepblue.deepblue.exception.GlobalExceptionHandler;
import com.deepblue.deepblue.exception.ResourceNotFoundException;
import com.deepblue.deepblue.service.RescueCaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RescueCaseController.class)
@Import(GlobalExceptionHandler.class)
class RescueCaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RescueCaseService service;

    // 1. GET RescueCase existente -> 200 OK
    @Test
    void shouldReturnRescueCaseByCode() throws Exception {
        RescueCaseResponse response = new RescueCaseResponse(
                1L,
                "RES-2026-001",
                LocalDate.of(2026, 8, 20),
                "Bahia Concha",
                RescueStatus.IN_REHABILITATION,
                "DB-CAR",
                "AN-2026-001"
        );

        when(service.findByCode("RES-2026-001")).thenReturn(response);

        mockMvc.perform(get("/api/rescue-cases/{code}", "RES-2026-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseCode").value("RES-2026-001"))
                .andExpect(jsonPath("$.status").value("IN_REHABILITATION"));

        verify(service).findByCode("RES-2026-001");
    }

    // 2. GET RescueCase inexistente -> 404 Not Found + ErrorResponse
    @Test
    void shouldReturn404WhenRescueCaseNotFound() throws Exception {
        when(service.findByCode("RES-999"))
                .thenThrow(new ResourceNotFoundException("Rescue case not found: RES-999"));

        mockMvc.perform(get("/api/rescue-cases/{code}", "RES-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Rescue case not found: RES-999"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).findByCode("RES-999");
    }

    // 3. GET RescueCases por status -> 200 OK
    @Test
    void shouldReturnRescueCasesByStatus() throws Exception {
        RescueCaseResponse response = new RescueCaseResponse(
                1L,
                "RES-2026-001",
                LocalDate.of(2026, 8, 20),
                "Bahia Concha",
                RescueStatus.IN_REHABILITATION,
                "DB-CAR",
                "AN-2026-001"
        );

        when(service.findByStatus(RescueStatus.IN_REHABILITATION)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/rescue-cases")
                        .param("status", "IN_REHABILITATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].caseCode").value("RES-2026-001"));

        verify(service).findByStatus(RescueStatus.IN_REHABILITATION);
    }

    // 4. GET status inválido (Query Parameter) -> 400 Bad Request + ErrorResponse
    @Test
    void shouldReturn400WhenQueryParameterIsInvalid() throws Exception {
        mockMvc.perform(get("/api/rescue-cases")
                        .param("status", "FLYING"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.details").isMap());
    }

    // 5. PATCH status válido -> 200 OK
    @Test
    void shouldUpdateRescueStatusSuccessfully() throws Exception {
        String jsonPayload = """
                {
                    "status": "RELEASED"
                }
                """;

        RescueCaseResponse response = new RescueCaseResponse(
                1L,
                "RES-2026-001",
                LocalDate.of(2026, 8, 20),
                "Bahia Concha",
                RescueStatus.RELEASED,
                "DB-CAR",
                "AN-2026-001"
        );

        when(service.changeStatus(eq("RES-2026-001"), any())).thenReturn(response);

        mockMvc.perform(patch("/api/rescue-cases/{code}/status", "RES-2026-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RELEASED"));

        verify(service).changeStatus(eq("RES-2026-001"), any());
    }

    // 6. PATCH request inválido (cuerpo vacío) -> 400 Bad Request + details
    @Test
    void shouldReturn400WhenPatchRequestIsInvalid() throws Exception {
        String emptyJsonPayload = "{}";

        mockMvc.perform(patch("/api/rescue-cases/{code}/status", "RES-2026-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyJsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.details").isMap());
    }

    // 7. PATCH transición inválida -> 409 Conflict + ErrorResponse
    @Test
    void shouldReturn409WhenStatusTransitionInvalid() throws Exception {
        String jsonPayload = """
                {
                    "status": "RELEASED"
                }
                """;

        when(service.changeStatus(eq("RES-2026-001"), any()))
                .thenThrow(new BusinessRuleException("Invalid status transition"));

        mockMvc.perform(patch("/api/rescue-cases/{code}/status", "RES-2026-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Invalid status transition"))
                .andExpect(jsonPath("$.details").isMap());
    }

    // 8. JSON enum inválido (ej. mandar un estado mal formado en el PATCH) -> 400 Bad Request
    @Test
    void shouldReturn400WhenJsonOrEnumIsInvalid() throws Exception {
        String invalidJson = """
                {
                    "status": "FLYING"
                }
                """;

        mockMvc.perform(patch("/api/rescue-cases/{code}/status", "RES-2026-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}