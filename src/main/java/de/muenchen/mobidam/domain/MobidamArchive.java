/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.mobidam.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import lombok.*;

/**
 * This class represents a the mobidam archive entity.
 * All files archived in S3 have a database entry.
 */
@Entity
@Table(name = "Archive", schema = "mdass3eai")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MobidamArchive extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(nullable = false)
    @NotEmpty
    private String bucket;
    @Column(nullable = false)
    @NotEmpty
    private String path;
    @Column(nullable = false)
    @NotEmpty
    private LocalDate creation;
    @Column(nullable = false)
    @NotEmpty
    private LocalDate expiration;

}
