package com.deepblue.deepblue.repository;
import com.deepblue.deepblue.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface AnimalRepository extends JpaRepository<Animal, Long> {
    Optional<Animal> findByAnimalCode(String animalCode);
    List<Animal> findByCommonNameContainingIgnoreCase(String commonName);
    List<Animal> findByRescueCaseStatus(RescueStatus status);
    List<Animal> findByRescueCaseRescueCenterCode(String centerCode);
    @Query("""
        select distinct a from Animal a
        join a.treatments t join t.specialist s join s.expertiseAreas e
        where a.rescueCase.status = :status and lower(e.name) = lower(:expertiseName)
        """)
    List<Animal> findDistinctByStatusAndSpecialistExpertise(@Param("status") RescueStatus status, @Param("expertiseName") String expertiseName);
}
