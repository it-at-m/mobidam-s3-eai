/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.mobidam.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

/**
 * This class represents a TheEntity.
 * <p>
 * The entity's content will be loaded according to the reference variable.
 * </p>
 */
// Definition of getter, setter, ...
@Entity
@Table(name = "Archive", schema = "mdass3eai")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MobidamArchive extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    @NotEmpty
    private String bucket;
    @Column(nullable = false)
    @NotEmpty
    private String path;
    @Column(nullable = false)
    @NotEmpty
    private LocalDate date;
    @Column(nullable = false)
    @NotEmpty
    private LocalDate expiration;

}
