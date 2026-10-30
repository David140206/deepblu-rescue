-- Centro de rescate
INSERT INTO rescue_centers (code, name, city)VALUES (
           'CENTER-001',
           'Centro de Rescate Marino DeepBlue',
           'Santa Marta'
       );

-- Caso
INSERT INTO rescue_cases (case_code,rescue_date,rescue_location,status, rescue_center_id)
VALUES (
           'RES-2026-100',
           '2026-09-15',
           'Playa Blanca, Santa Marta',
           'IN_REHABILITATION',
           (SELECT id FROM rescue_centers WHERE code = 'CENTER-001')
       );

-- Animal
INSERT INTO animals (animal_code,common_name,scientific_name,sex, rescue_case_id,tracking_device_code)
VALUES (
           'AN-2026-100',
           'Green Sea Turtle',
           'Chelonia mydas',
           'FEMALE',
           (SELECT id FROM rescue_cases WHERE case_code = 'RES-2026-100'),
           'TRACK-2026-100'
       );

-- Historia clínica del animal
INSERT INTO medical_records (animal_id, initial_weight,initial_condition, injuries, observations)
VALUES (
           (SELECT id FROM animals WHERE animal_code = 'AN-2026-100'),
           18.50,
           'Under rehabilitation',
           'Minor injuries to the front flippers',
           'Requires monitoring and follow-up rehabilitation'
       );

-- Especialista
INSERT INTO specialists (professional_code,first_name,last_name,email,active)
VALUES (
           'SPEC-001',
           'Elena',
           'Vargas',
           'elena.vargas@deepblue.example',
           TRUE
       );

-- Especialidades del especialista
INSERT INTO specialist_expertise (specialist_id, expertise_id)
SELECT s.id, e.id
FROM specialists s
         CROSS JOIN expertise e
WHERE s.professional_code = 'SPEC-001'
  AND e.name IN ('Marine Reptiles', 'Rehabilitation');

-- Segundo caso
INSERT INTO rescue_cases (case_code,rescue_date, rescue_location,status, rescue_center_id)
VALUES (
           'RES-2026-101',
           '2026-10-20',
           'Playa Salguero, Santa Marta',
           'ADMITTED',
           (SELECT id FROM rescue_centers WHERE code = 'CENTER-001')
       );

-- Segundo animal
INSERT INTO animals (animal_code,common_name, scientific_name, sex, rescue_case_id, tracking_device_code)
VALUES (
           'AN-2026-101',
           'Hawksbill Sea Turtle',
           'Eretmochelys imbricata',
           'UNKNOWN',
           (SELECT id FROM rescue_cases WHERE case_code = 'RES-2026-101'),
           'TRACK-2026-101'
       );

-- Historia clínica del segundo animal
INSERT INTO medical_records (animal_id,initial_weight,initial_condition,injuries, observations)
VALUES (
           (SELECT id FROM animals WHERE animal_code = 'AN-2026-101'),
           12.30,
           'Under initial evaluation',
           'No injuries recorded yet',
           'New admission; requires initial assessment'
       );
