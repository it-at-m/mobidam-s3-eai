package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.rest.ViewBucketContent200ResponseInner;
import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

@Component
public class FilesInFolderConverter implements TypeConverters {

    @Converter
   public ViewBucketContent200ResponseInner[] convert(Collection<S3Object> objects){

        var files = new ArrayList<ViewBucketContent200ResponseInner>();

        objects.forEach(s3object -> {
            var file = new ViewBucketContent200ResponseInner();
            file.setKey(s3object.key());
            file.setLastmodified(s3object.lastModified().toString());
            file.setSize(new BigDecimal(s3object.size()));
            files.add(file);

        });
        return files.toArray(ViewBucketContent200ResponseInner[]::new);
    }

}
