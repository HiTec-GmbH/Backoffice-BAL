package de.szut.pms.projectemployee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ProjectEmployeeRepository extends JpaRepository<ProjectEmployeeEntity, Long> {
    
    
    List<ProjectEmployeeEntity> findAllByEmployeeId(Long employeeId);

    @Query("""
        SELECT 
            COUNT(pe) > 0 
        FROM 
            ProjectEmployeeEntity pe
        WHERE 
            pe.employeeId = :employeeId
        AND pe.project.id = :projectId
        """)
    boolean isAssigned(
        @Param("employeeId") Long employeeId,
        @Param("projectId") Long projectId);
    
    @Query("""
        SELECT 
            pe 
        FROM 
            ProjectEmployeeEntity pe 
        JOIN 
            pe.project pj 
        WHERE 
            pe.employeeId = :employeeId 
        AND pj.id != :projectId 
        AND pj.startDate <= :endDate 
        AND pj.endDate >= :startDate
        """)
    List<ProjectEmployeeEntity> findOverlapping(
        @Param("employeeId") Long employeeId,
        @Param("projectId") Long projectId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);
}
