# DeepBlue Rescue

Persistencia para una plataforma de rescate y rehabilitación de fauna marina, creada con Java 21, Spring Boot, Spring Data JPA, PostgreSQL, Flyway y Testcontainers.

## Modelo y relaciones

- `RescueCenter` 1:N `RescueCase`.
- `RescueCase` 1:1 `Animal`; la FK única está en `animals.rescue_case_id`.
- `Animal` 1:1 `MedicalRecord`; la FK única está en `medical_records.animal_id`.
- `Specialist` N:M `Expertise`, mediante `specialist_expertise`.
- `Animal` 1:N `Treatment` y `Specialist` 1:N `Treatment`.

## Ejecución

Configura PostgreSQL local con las variables opcionales `DB_URL`, `DB_USER` y `DB_PASSWORD`, o usa los valores por defecto de `application.yaml`. Después ejecuta:

```bash
./mvnw test
```

Los tests inician PostgreSQL real mediante Testcontainers; Docker debe estar disponible. Flyway aplica V1, V2 y V3 antes de que Hibernate valide el esquema con `ddl-auto: validate`.

## Consultas

Los repositorios incluyen Query Methods para códigos, estados, fechas y navegación entre asociaciones, además de JPQL para especialistas por experiencia, tratamientos por intervalo/centro/experiencia y el reto de animales en rehabilitación tratados por especialistas con una experiencia dada.

La prueba dedicada de `flyway_schema_history` (paso 47 del laboratorio) fue omitida intencionalmente.
