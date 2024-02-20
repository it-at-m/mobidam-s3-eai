package de.muenchen.mobidam.common;

import org.springframework.stereotype.Component;

@Component
public class EnvironmentReader {

    public String getEnvironmentVariable(final String key) {
        return System.getenv(key);
    }
}
