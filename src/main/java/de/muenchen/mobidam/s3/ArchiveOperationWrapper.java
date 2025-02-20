/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.domain.MobidamArchive;
import de.muenchen.mobidam.eai.common.CommonConstants;
import de.muenchen.mobidam.eai.common.exception.CommonError;
import de.muenchen.mobidam.eai.common.exception.ErrorResponseBuilder;
import de.muenchen.mobidam.eai.common.exception.MobidamException;
import de.muenchen.mobidam.rest.BucketContentInner;
import de.muenchen.mobidam.service.ArchiveService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ArchiveOperationWrapper implements Processor {

    @Value("${mobidam.archive.expiration-months:1}")
    private int archiveExpiration;

    private final ArchiveService archiveService;

    public ArchiveOperationWrapper(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        var filesInArchive = archiveService.getObjectsFoundInContainer(exchange);

        var bucketName = exchange.getIn().getHeader(CommonConstants.HEADER_BUCKET_NAME, String.class);

        exchange.getIn().removeHeader(AWS2S3Constants.S3_OPERATION);

        var objectName = exchange.getIn().getHeader(Constants.PARAMETER_OBJECT_NAME, String.class);
        if (objectName == null) {
            CommonError error = ErrorResponseBuilder.build(HttpStatus.BAD_REQUEST.value(), "Object name is empty");
            exchange.getMessage().setBody(error);
            throw new MobidamException("Object name is empty");
        }

        var archiveObjectName = processObjectName(filesInArchive, Constants.ARCHIVE_PATH + objectName);

        exchange.getIn().setHeader(AWS2S3Constants.BUCKET_DESTINATION_NAME, bucketName);
        exchange.getIn().setHeader(AWS2S3Constants.KEY, objectName);
        exchange.getIn().setHeader(AWS2S3Constants.DESTINATION_KEY, archiveObjectName);

        var archiveEntity = new MobidamArchive();
        archiveEntity.setBucket(bucketName);
        archiveEntity.setPath(exchange.getIn().getHeader(AWS2S3Constants.DESTINATION_KEY, String.class));
        archiveEntity.setCreation(LocalDate.now());
        archiveEntity.setExpiration(LocalDate.now().plusMonths(archiveExpiration));
        exchange.setProperty(Constants.ARCHIVE_ENTITY, archiveEntity);

    }

    private String processObjectName(ArrayList<BucketContentInner> files, String objectName) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String formattedNow = now.format(formatter);

        for (BucketContentInner file : files) {
            if (Objects.equals(file.getKey(), objectName)) {
                String baseName = FilenameUtils.getBaseName(objectName);
                String extension = FilenameUtils.getExtension(objectName);
                objectName = String.format("%s_%s.%s", baseName, formattedNow, extension);
                break;
            }
        }

        return objectName;
    }
}
