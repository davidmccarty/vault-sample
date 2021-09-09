package com.garage.vault;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.vault.config.SecretBackendConfigurer;
import org.springframework.cloud.vault.config.VaultConfigurer;

public class VaultConfig implements VaultConfigurer {

    @Autowired(required = true)
    private VaultCustomProperties properties;

    @Override
    public void addSecretBackends(final SecretBackendConfigurer configurer) {
        properties.getPaths().forEach(configurer::add);
        configurer.registerDefaultKeyValueSecretBackends(false);
        configurer.registerDefaultDiscoveredSecretBackends(true);
    }

}
