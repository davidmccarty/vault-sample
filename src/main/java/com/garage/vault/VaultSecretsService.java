package com.garage.vault;

import java.net.URISyntaxException;
import java.util.Base64;

import com.garage.model.KeyValuePair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponseSupport;

@Service
public class VaultSecretsService {

    private static final Logger LOG = LoggerFactory.getLogger(VaultSecretsService.class);

    @Autowired
    private VaultTemplate vaultTemplate;

    public void putSecret(String path, Object secret) throws URISyntaxException {
        vaultTemplate.write(path, secret);
        LOG.debug("Vault put secret successful on path {}", path);
    }

    public <T> T getSecret(String path, Class<T> clazz) throws URISyntaxException {
        VaultResponseSupport<T> response = vaultTemplate.read(path, clazz);
        LOG.debug("Vault get secret successful on path {}", path);
        return response.getData();
    }

    public void putSecretFile(String path, String key, byte[] bytes) throws URISyntaxException {
        String value = Base64.getEncoder().encodeToString(bytes);
        KeyValuePair secret = new KeyValuePair(key, value);
        putSecret(path, secret);
    }

    public byte[] gettSecretFile(String path, String key) throws URISyntaxException {
        KeyValuePair secret = getSecret(path, KeyValuePair.class);
        byte[] bytes = Base64.getDecoder().decode(secret.getValue());
        return bytes;
    }

}
