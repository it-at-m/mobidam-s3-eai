package de.muenchen.mobidam.service;

import de.muenchen.mobidam.domain.MobidamArchive;
import de.muenchen.mobidam.repository.ArchiveRepository;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ArchiveService {

    @Value("${mobidam.limit.search.items:20}")
    private int maxS3ObjectItems;

    private final ArchiveRepository archiveRepository;

    public ArchiveService(ArchiveRepository archiveRepository) {
        this.archiveRepository = archiveRepository;
    }

    public MobidamArchive save(MobidamArchive entity) {
        return archiveRepository.saveAndFlush(entity);
    }

    public Iterable<MobidamArchive> listExpired() {
        return archiveRepository.findAllByExpirationBefore(LocalDate.now());
    }

    public void delete(MobidamArchive entity) {
        archiveRepository.delete(entity);
    }

}
