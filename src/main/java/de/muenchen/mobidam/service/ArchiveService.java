package de.muenchen.mobidam.service;

import de.muenchen.mobidam.domain.MobidamArchive;
import de.muenchen.mobidam.repository.ArchiveRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class ArchiveService {

    private final ArchiveRepository archiveRepository;

    public MobidamArchive save(MobidamArchive entity) {
        return archiveRepository.saveAndFlush(entity);
    }

}
