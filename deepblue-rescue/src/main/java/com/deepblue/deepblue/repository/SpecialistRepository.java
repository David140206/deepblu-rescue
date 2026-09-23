package com.deepblue.deepblue.repository;
import com.deepblue.deepblue.domain.Specialist;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SpecialistRepository extends JpaRepository<Specialist, Long> {
    Optional<Specialist> findByProfessionalCode(
            String professionalCode);
    @Query("""
        select distinct s from Specialist s join s.expertiseAreas e
        where s.active = true and lower(e.name) = lower(:expertiseName)
        order by s.lastName asc, s.firstName asc
        """)
    List<Specialist> findActiveByExpertise(@Param("expertiseName") String expertiseName);
}
