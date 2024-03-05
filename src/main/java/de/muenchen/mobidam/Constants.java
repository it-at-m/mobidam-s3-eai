package de.muenchen.mobidam;

public class Constants {

    public static final String MOBIDAM_LOGGER = "de.muenchen.mobidam";
    public static final String CAMEL_SERVLET_CONTEXT_PATH = "CamelServletContextPath";

    /*
     * Must be compatible with parameters in resource/openapi_rest_s3_v1.yaml
     */
    public static final String CAMEL_SERVLET_CONTEXT_PATH_FILES_IN_FOLDER = "/filesInFolder";
    public static final String CAMEL_SERVLET_CONTEXT_PATH_PRESIGNED_URL = "/presignedUrl";
    public static final String PATH_ALIAS_PREFIX = "path";

    public static final String BUCKET_NAME = "bucketName";
    public static final String OBJECT_NAME = "objectName";

    // Headers for bucket credentials
    public static final String ACCESS_KEY = "accessKey";
    public static final String SECRET_KEY = "secretKey";
}
