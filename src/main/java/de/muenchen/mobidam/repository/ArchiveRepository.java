package de.muenchen.mobidam.repository;

import de.muenchen.mobidam.domain.MobidamArchive;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchiveRepository extends JpaRepository<MobidamArchive, UUID> {

    Iterable<MobidamArchive> findAllByExpirationBefore(LocalDate now);

}
