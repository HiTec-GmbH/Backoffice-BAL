package de.szut.pms.project;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String customerContact;

    @Column(nullable = false)
    private Long projectLeaderId;

    @Column(nullable = false)
    private String comment;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false) //TODO: geplantes Endate und tatsächliches?
    private LocalDate endDate;
}