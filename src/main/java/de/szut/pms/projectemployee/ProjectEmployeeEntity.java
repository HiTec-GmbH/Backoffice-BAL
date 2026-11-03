package de.szut.pms.projectemployee;

import de.szut.pms.project.ProjectEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
// Sicherheitsnetz zu ensureNotAssigned (BAL-008 A_7), greift auch bei gleichzeitigen Anfragen
@Table(name = "project_employee", uniqueConstraints = @UniqueConstraint(columnNames = { "project_id", "employee_id" }))
@Getter
@Setter
@NoArgsConstructor
public class ProjectEmployeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String qualification;

    private Long employeeId;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    public ProjectEmployeeEntity(String qualification, Long employeeId) {
        this.qualification = qualification;
        this.employeeId = employeeId;
    }
}
