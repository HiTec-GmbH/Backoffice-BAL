package de.szut.pms.project;

import java.time.LocalDate;

public record ProjectDto(

        Long id,
        String name,
        Long customerId,
        String customerContact,
        Long projectLeaderId,
        String comment,
        LocalDate startDate,
        LocalDate endDate

) {
}