package com.deepblue.deepblue.controller;

import com.deepblue.deepblue.exception.GlobalExceptionHandler;
import com.deepblue.deepblue.service.AnimalService;
import com.deepblue.deepblue.service.TreatmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnimalController.class)
@Import(GlobalExceptionHandler.class)
class AnimalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnimalService animalService;

    @MockitoBean
    private TreatmentService treatmentService;

    @Test
    void shouldReturnAnimalByCode() throws Exception {
        mockMvc.perform(get("/api/animals/{animalCode}", "AN-001"))
                .andExpect(status().isOk());

        verify(animalService).findByCode("AN-001");
    }

    @Test
    void shouldReturnAnimalsInRehabilitation() throws Exception {
        when(animalService.findAnimalsInRehabilitation()).thenReturn(List.of());

        mockMvc.perform(get("/api/animals/in-rehabilitation"))
                .andExpect(status().isOk());

        verify(animalService).findAnimalsInRehabilitation();
    }

    @Test
    void shouldReturnAnimalTreatments() throws Exception {
        mockMvc.perform(get("/api/animals/{animalCode}/treatments", "AN-001"))
                .andExpect(status().isOk());

        verify(treatmentService).findByAnimalCode("AN-001");
    }

    @Test
    void shouldReturnTreatmentEligibility() throws Exception {
        when(animalService.canReceiveTreatment("AN-001")).thenReturn(true);

        mockMvc.perform(get("/api/animals/{animalCode}/treatment-eligibility", "AN-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animalCode").value("AN-001"))
                .andExpect(jsonPath("$.eligible").value(true));

        verify(animalService).canReceiveTreatment("AN-001");
    }
    @Test
    void shouldReturn404WhenAnimalNotFound() throws Exception {
        // Configurar el mock para que lance la excepción cuando busquen un animal inexistente
        when(animalService.findByCode("AN-999"))
                .thenThrow(new com.deepblue.deepblue.exception.ResourceNotFoundException("Animal not found: AN-999"));

        mockMvc.perform(get("/api/animals/{animalCode}", "AN-999"))
                .andExpect(status().isNotFound()) // Valida código 404
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Animal not found: AN-999"))
                .andExpect(jsonPath("$.details").isMap());

        verify(animalService).findByCode("AN-999");
    }
    // 18. Error inesperado -> 500 Internal Server Error
    @Test
    void shouldReturn500WhenUnexpectedErrorOccurs() throws Exception {
        when(animalService.findByCode("AN-001"))
                .thenThrow(new RuntimeException("Unexpected database error"));

        mockMvc.perform(get("/api/animals/{animalCode}", "AN-001"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.details").isMap());

        verify(animalService).findByCode("AN-001");
    }
}