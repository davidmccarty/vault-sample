package com.garage.vault;

import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponseSupport;

@Service
public class VaultSecretsService {
    @Autowired
    private VaultTemplate vaultTemplate;

    public void putSecret(String path, Object secret) throws URISyntaxException {
        vaultTemplate.write(path, secret);
    }

    public <T> T getSecret(String path, Class<T> clazz) throws URISyntaxException {
        VaultResponseSupport<T> response = vaultTemplate.read(path, clazz);
        System.out.println(response.getData());
        return response.getData();
    }
}
