/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.domain.MobidamArchive;
import de.muenchen.mobidam.eai.common.CommonConstants;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ArchiveOperationWrapper extends OperationWrapper {

    @Value("${mobidam.archive.expiration-months:1}")
    private int archiveExpiration;

    private final String formattedDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

    @Override
    public void process(Exchange exchange) throws Exception {
        var file = exchange.getIn().getBody(Collection.class);
        var bucketName = exchange.getIn().getHeader(CommonConstants.HEADER_BUCKET_NAME, String.class);

        exchange.getIn().removeHeader(AWS2S3Constants.S3_OPERATION);

        var objectName = exchange.getIn().getHeader(Constants.PARAMETER_OBJECT_NAME, String.class);
        exchange = checkObjectName(exchange, objectName);

        var archiveObjectName = Constants.ARCHIVE_PATH + objectName;

        if (!file.isEmpty()) {
            archiveObjectName = processObjectName(archiveObjectName);
        }

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

    private String processObjectName(String objectName) {
        log.info("Multiple files with identical name ({}) in the archive.", objectName);

        return String.format("%s%s_%s.%s", FilenameUtils.getFullPath(objectName), FilenameUtils.getBaseName(objectName), formattedDateTime,
                FilenameUtils.getExtension(objectName));
    }
}
