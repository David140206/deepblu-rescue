package com.deepblue.deepblue.repository;

import com.deepblue.deepblue.domain.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
}
