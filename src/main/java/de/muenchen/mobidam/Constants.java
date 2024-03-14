package de.muenchen.mobidam;

public class Constants {

    public static final String MOBIDAM_LOGGER = "de.muenchen.mobidam";
    public static final String CAMEL_SERVLET_CONTEXT_PATH = "CamelServletContextPath";
    public static final String ARCHIVE_ENTITY = "ArchiveEntity";

    /*
     * Must be compatible with parameters in resource/openapi_rest_s3_v1.yaml
     */
    public static final String CAMEL_SERVLET_CONTEXT_PATH_FILES_IN_FOLDER = "/filesInFolder";
    public static final String CAMEL_SERVLET_CONTEXT_PATH_PRESIGNED_URL = "/presignedUrl";
    public static final String CAMEL_SERVLET_CONTEXT_PATH_ARCHIVE = "/archive";
    public static final String PREFIX_ARCHIVE = "archived";
    public static final String BUCKET_NAME = "bucketName";
    public static final String OBJECT_NAME = "objectName";

    // S3 url parameter
    public static final String S3_PREFIX = "prefix=";
    public static final String S3_DELIMITER = "delimiter=";

    // Headers for bucket credentials
    public static final String ACCESS_KEY = "accessKey";
    public static final String SECRET_KEY = "secretKey";
}
