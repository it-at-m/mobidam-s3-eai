package de.muenchen.mobidam.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.net.URISyntaxException;


@Configuration
public class S3ClientConfiguration {

    @Value("${camel.component.aws2-s3.access-key}")
    private String s3AccessKey;

    @Value("${camel.component.aws2-s3.secret-key}")
    private String s3SecretAccessKey;

    @Value("${camel.component.aws2-s3.override-endpoint}")
    private String s3url;

    @Value("${camel.component.aws2-s3.region}")
    private Region s3Region;

     @Bean
    public S3Client s3Client() throws URISyntaxException {
        return  S3Client.builder().endpointOverride(new URI(s3url)).region(s3Region).credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessKey, s3SecretAccessKey))).build();
    }

}
