package com.deepblue.deepblue;

import com.deepblue.deepblue.domain.*;
import com.deepblue.deepblue.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
@Transactional
class PersistenceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("deepblue_test")
            .withUsername("deepblue")
            .withPassword("deepblue");

    @Autowired
    RescueCenterRepository rescueCenterRepository;

    @Autowired
    RescueCaseRepository rescueCaseRepository;

    @Autowired
    AnimalRepository animalRepository;

    @Autowired
    SpecialistRepository specialistRepository;

    @Autowired
    ExpertiseRepository expertiseRepository;

    @Autowired
    TreatmentRepository treatmentRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    // Paso 48
    @Test
    void inheritedRepositoryMethodsWork() {
        RescueCenter center = rescueCenterRepository.saveAndFlush(
                new RescueCenter("DB-CAR", "DeepBlue Caribbean Center", "Santa Marta"));

        assertThat(rescueCenterRepository.findById(center.getId())).isPresent();
        assertThat(rescueCenterRepository.existsById(center.getId())).isTrue();
        assertThat(rescueCenterRepository.count()).isEqualTo(1);
    }

    // Paso 49
    @Test
    void rescueCenterOneToManyWorks() {
        RescueCenter center = new RescueCenter("DB-CAR", "DeepBlue Caribbean", "Santa Marta");
        center.addCase(new RescueCase("RES-001", LocalDate.of(2026, 8, 1), "Bay", RescueStatus.ADMITTED));
        center.addCase(new RescueCase("RES-002", LocalDate.of(2026, 8, 2), "Port", RescueStatus.UNDER_EVALUATION));
        rescueCenterRepository.saveAndFlush(center);

        assertThat(rescueCaseRepository.findByRescueCenterCode("DB-CAR")).hasSize(2);
    }

    // Paso 50
    @Test
    void rescueCaseOneToOneAnimalWorks() {
        Animal animal = persistAnimal("RES-2026-001", "AN-2026-001", "DB-CAR", RescueStatus.ADMITTED);

        assertThat(animal.getRescueCase().getAnimal()).isSameAs(animal);
        assertThat(animal.getRescueCase()).isNotNull();
    }

    // Paso 51
    @Test
    void animalOneToOneMedicalRecordWorks() {
        Animal animal = persistAnimal("RES-2026-002", "AN-2026-002", "DB-CAR", RescueStatus.ADMITTED);
        MedicalRecord record = new MedicalRecord(new BigDecimal("28.40"), "STABLE", "Left front flipper injury", null);
        animal.assignMedicalRecord(record);
        animalRepository.saveAndFlush(animal);

        assertThat(animal.getId()).isNotNull();
        assertThat(record.getId()).isNotNull();
    }

    // Paso 52
    @Test
    void specialistManyToManyWorks() {
        Specialist elena = specialist("SPEC-001", "Elena", "Vargas", "elena@deepblue.org", "Trauma", "Rehabilitation");
        specialistRepository.saveAndFlush(elena);

        assertThat(elena.getExpertiseAreas()).hasSize(2);
    }

    // Pasos 53 y 54
    @Test
    void queryMethodsForCasesAndAnimalsWork() {
        persistAnimal("RES-001", "AN-001", "DB-CAR", RescueStatus.IN_REHABILITATION);
        persistAnimal("RES-002", "AN-002", "DB-CAR", RescueStatus.READY_FOR_RELEASE);
        persistAnimal("RES-003", "AN-003", "DB-PAC", RescueStatus.IN_REHABILITATION);

        assertThat(rescueCaseRepository.findByStatusOrderByRescueDateAsc(RescueStatus.IN_REHABILITATION)).hasSize(2);
        assertThat(animalRepository.findByRescueCaseRescueCenterCode("DB-CAR")).hasSize(2);
        assertThat(animalRepository.findByRescueCaseRescueCenterCode("DB-CAR"))
                .extracting(Animal::getAnimalCode)
                .doesNotContain("AN-003");
    }

    // Paso 55
    @Test
    void specialistJpqlByExpertiseWorks() {
        specialistRepository.saveAllAndFlush(List.of(
                specialist("SPEC-ELENA", "Elena", "Vargas", "elena@deepblue.org", "Trauma", "Rehabilitation"),
                specialist("SPEC-MATEO", "Mateo", "Lopez", "mateo@deepblue.org", "Marine Mammals", "Rehabilitation"),
                specialist("SPEC-SOFIA", "Sofia", "Ruiz", "sofia@deepblue.org", "Marine Birds", "Trauma")));

        assertThat(specialistRepository.findActiveByExpertise("Trauma"))
                .extracting(Specialist::getFirstName)
                .containsExactly("Sofia", "Elena");
    }

    // Pasos 56 y 57
    @Test
    void treatmentQueryMethodReturnsChronologicalTreatments() {
        Animal animal = persistAnimal("RES-100", "AN-100", "DB-CAR", RescueStatus.IN_REHABILITATION);
        Specialist elena = specialistRepository.saveAndFlush(specialist("SPEC-100", "Elena", "Vargas", "elena100@deepblue.org", "Trauma"));
        LocalDateTime time = LocalDateTime.of(2026, 8, 1, 10, 0);
        treatmentRepository.saveAllAndFlush(List.of(
                new Treatment(animal, elena, time, TreatmentType.WOUND_CARE, "Treatment 1"),
                new Treatment(animal, elena, time.plusDays(1), TreatmentType.HYDRATION, "Treatment 2"),
                new Treatment(animal, elena, time.plusDays(2), TreatmentType.OBSERVATION, "Treatment 3")));

        assertThat(treatmentRepository.findByAnimalIdOrderByPerformedAtAsc(animal.getId()))
                .extracting(Treatment::getDescription)
                .containsExactly("Treatment 1", "Treatment 2", "Treatment 3");
    }

    // Paso 58
    @Test
    void treatmentJpqlByIntervalWorks() {
        Animal animal = persistAnimal("RES-200", "AN-200", "DB-CAR", RescueStatus.ADMITTED);
        Specialist specialist = specialistRepository.saveAndFlush(specialist("SPEC-200", "Elena", "Vargas", "elena200@deepblue.org", "Trauma"));
        treatmentRepository.saveAllAndFlush(List.of(
                new Treatment(animal, specialist, LocalDateTime.of(2026, 8, 1, 10, 0), TreatmentType.WOUND_CARE, "First"),
                new Treatment(animal, specialist, LocalDateTime.of(2026, 8, 10, 10, 0), TreatmentType.HYDRATION, "Middle"),
                new Treatment(animal, specialist, LocalDateTime.of(2026, 8, 20, 10, 0), TreatmentType.OBSERVATION, "Last")));

        assertThat(treatmentRepository.findPerformedBetween(LocalDateTime.of(2026, 8, 5, 0, 0), LocalDateTime.of(2026, 8, 15, 0, 0)))
                .extracting(Treatment::getDescription)
                .containsExactly("Middle");
    }

    // Paso 59
    @Test
    void uniqueConstraintRejectsDuplicateAnimalCode() {
        persistAnimal("RES-300", "AN-UNIQUE", "DB-CAR", RescueStatus.ADMITTED);

        assertThatThrownBy(() -> persistAnimal("RES-301", "AN-UNIQUE", "DB-CAR", RescueStatus.ADMITTED))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Animal persistAnimal(String caseCode, String animalCode, String centerCode, RescueStatus status) {
        RescueCenter center = rescueCenterRepository.findByCode(centerCode)
                .orElseGet(() -> rescueCenterRepository.save(new RescueCenter(centerCode, "Center " + centerCode, "Santa Marta")));
        RescueCase rescueCase = new RescueCase(caseCode, LocalDate.of(2026, 8, 18), "Bahia Concha", status);
        Animal animal = new Animal(animalCode, "Green Sea Turtle", "Chelonia mydas", AnimalSex.FEMALE);
        rescueCase.assignAnimal(animal);
        center.addCase(rescueCase);
        rescueCenterRepository.saveAndFlush(center);
        return animal;
    }

    private Specialist specialist(String code, String firstName, String lastName, String email, String... expertiseNames) {
        Specialist specialist = new Specialist(code, firstName, lastName, email, true);
        for (String expertiseName : expertiseNames) {
            specialist.addExpertise(expertiseRepository.findByNameIgnoreCase(expertiseName).orElseThrow());
        }
        return specialist;
    }

    // Parte XIII

    @Test
    void integratorScenarioPersistenceAndQueriesWork() {

        // Paso 65
        Expertise marineReptiles = expertiseRepository.findByNameIgnoreCase("Marine Reptiles").orElseThrow();
        Expertise trauma = expertiseRepository.findByNameIgnoreCase("Trauma").orElseThrow();
        Expertise rehabilitation = expertiseRepository.findByNameIgnoreCase("Rehabilitation").orElseThrow();

        RescueCenter center = new RescueCenter("DB-CAR", "DeepBlue Caribbean", "Santa Marta");
        rescueCenterRepository.saveAndFlush(center);

        RescueCase rescueCase = new RescueCase("RES-2026-100", LocalDate.of(2026, 8, 18), "Bahia Concha", RescueStatus.IN_REHABILITATION);

        Animal animal = new Animal("AN-2026-100", "Green Sea Turtle", "Chelonia mydas", AnimalSex.FEMALE);

        MedicalRecord medicalRecord = new MedicalRecord(new BigDecimal("27.80"), "STABLE", "Injury caused by fishing net", "Possible plastic ingestion");
        animal.assignMedicalRecord(medicalRecord);
        rescueCase.assignAnimal(animal);
        center.addCase(rescueCase);
        rescueCenterRepository.saveAndFlush(center);

        Specialist specialist = new Specialist("SPEC-001", "Elena", "Vargas", "elena@deepblue.org", true);
        specialist.addExpertise(marineReptiles);
        specialist.addExpertise(trauma);
        specialist.addExpertise(rehabilitation);
        specialistRepository.saveAndFlush(specialist);

        Treatment treatment1 = new Treatment(animal, specialist, LocalDateTime.of(2026, 8, 18, 10, 0), TreatmentType.WOUND_CARE, "Cleaning of left front flipper");
        treatmentRepository.saveAndFlush(treatment1);

        Treatment treatment2 = new Treatment(animal, specialist, LocalDateTime.of(2026, 8, 19, 10, 0), TreatmentType.HYDRATION, "Subcutaneous fluid therapy");
        treatmentRepository.saveAndFlush(treatment2);


        // Paso 66
        RescueCase foundCase = rescueCaseRepository.findByCaseCode("RES-2026-100").orElseThrow();
        assertThat(foundCase.getCaseCode()).isEqualTo("RES-2026-100");

        List<RescueCase> rehabilitationCases = rescueCaseRepository.findByStatusOrderByRescueDateAsc(RescueStatus.IN_REHABILITATION);
        assertThat(rehabilitationCases).extracting(RescueCase::getCaseCode).contains("RES-2026-100");

        List<Animal> centerAnimals = animalRepository.findByRescueCaseRescueCenterCode("DB-CAR");
        assertThat(centerAnimals).extracting(Animal::getAnimalCode).contains("AN-2026-100");

        List<Animal> turtles = animalRepository.findByCommonNameContainingIgnoreCase("turtle");
        assertThat(turtles).extracting(Animal::getAnimalCode).contains("AN-2026-100");

        List<Specialist> traumaSpecialists = specialistRepository.findActiveByExpertise("Trauma");
        assertThat(traumaSpecialists).extracting(Specialist::getProfessionalCode).contains("SPEC-001");

        List<Treatment> animalTreatments = treatmentRepository.findByAnimalIdOrderByPerformedAtAsc(animal.getId());
        assertThat(animalTreatments).hasSize(2);

        List<Treatment> rehabilitationTreatments = treatmentRepository.findBySpecialistExpertise("Rehabilitation");
        assertThat(rehabilitationTreatments).hasSize(2);

        List<Treatment> treatmentsBetween = treatmentRepository.findPerformedBetween(LocalDateTime.of(2026, 8, 17, 0, 0), LocalDateTime.of(2026, 8, 20, 0, 0));
        assertThat(treatmentsBetween).hasSize(2);
    }
}
