package de.muenchen.mobidam.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentReader {

    @Autowired
    private Environment environment;

    public String getEnvironmentVariable(final String key) {
        return this.environment.getProperty(key);
    }
}
