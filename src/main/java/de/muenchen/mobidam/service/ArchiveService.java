package de.muenchen.mobidam.service;

import de.muenchen.mobidam.domain.MobidamArchive;
import de.muenchen.mobidam.repository.ArchiveRepository;
import de.muenchen.mobidam.rest.BucketContentInner;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.S3Object;

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

    public ArrayList<BucketContentInner> getObjectsFoundInContainer(Exchange exchange) {

        var objects = exchange.getIn().getBody(Collection.class);

        if (objects.size() > maxS3ObjectItems) {
            log.warn("More than {} objects in storage", maxS3ObjectItems);
        }

        var files = new ArrayList<BucketContentInner>();

        objects.stream().limit(maxS3ObjectItems).forEach(object -> {
            var s3Object = (S3Object) object;
            BucketContentInner content = new BucketContentInner();
            content.setKey(s3Object.key());
            content.setLastmodified(s3Object.lastModified().toString());
            content.setSize(BigDecimal.valueOf(s3Object.size()));
            files.add(content);
        });

        return files;
    }

}
