package de.muenchen.mobidam.s3;

import io.minio.MinioClient;
import io.minio.credentials.Provider;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class S3ClientConfiguration {

    @Value("${camel.component.minio.access-key}")
    private String s3AccessKey;

    @Value("${camel.component.minio.secret-key}")
    private String s3SecretAccessKey;

//    @Value("${s3.bucket}")
//    private String bucket;

    @Value("${camel.component.minio.endpoint}")
    private String s3url;

    @Autowired
    private CamelContext camelContext;

    @Bean
    public MinioClient minioClient() {

        return MinioClient.builder().endpoint(s3url).region("us-east-1").credentials(s3AccessKey, s3SecretAccessKey).build();

    }


//    public S3Client s3Client() throws URISyntaxException {
//
////        final AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(s3Endpoint, REGION);
////        final AmazonS3 client = AmazonS3ClientBuilder.standard()
////                .withEndpointConfiguration(endpoint)
////                .build();
//
//
//    //return  S3Client.builder().endpointOverride(new URI(s3url)).credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessKey, s3SecretAccessKey))).region(Region.EU_WEST_1).build();
//        //return  S3Client.builder().endpointOverride(new URI(s3url)).credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessKey, s3SecretAccessKey))).build();
//        return  S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(s3AccessKey, s3SecretAccessKey))).build();
//    }
//
//    @Bean
//    public AWS2S3Configuration aws2S3Configuration() throws URISyntaxException {
//        AWS2S3Configuration configuration = new AWS2S3Configuration();
//        configuration.setAmazonS3Client(s3Client());
//        //configuration.setAutoDiscoverClient(true);
//        //configuration.setBucketName(bucket);
//        configuration.setRegion(Region.EU_WEST_1.id());
//        configuration.setUseDefaultCredentialsProvider(false);
//        configuration.setUriEndpointOverride(s3url);
//        configuration.setOverrideEndpoint(true);
//
//        return configuration;
//    }
//
//    @Bean
//    public AWS2S3Component aws2s3() throws URISyntaxException {
//
//        AWS2S3Component s3Component = new AWS2S3Component(camelContext);
//        s3Component.setConfiguration(aws2S3Configuration());
//        s3Component.setLazyStartProducer(true);
//        return s3Component;
//
//    }

}
