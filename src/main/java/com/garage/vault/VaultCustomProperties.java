package com.garage.vault;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.cloud.vault")
public class VaultCustomProperties {
    private final List<String> paths = new ArrayList<>();
    public List<String> getPaths() {
        return paths;
    }
}
