package com.garage.vault;

import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Base64;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.RawTransitKey;
import org.springframework.vault.support.TransitKeyType;
import org.springframework.vault.support.VaultTransitKey;
import org.springframework.vault.support.VaultTransitKeyCreationRequest;

@Service
public class VaultTransitService {

    private static final Logger LOG = LoggerFactory.getLogger(VaultTransitService.class);

    @Autowired
    private VaultOperations vaultOperations;

    @PostConstruct
    public void setProperty() {
        Security.setProperty("crypto.policy", "unlimited");
    }

    public int checkEncryptionPolicy() throws NoSuchAlgorithmException{
        int maxKeySize = javax.crypto.Cipher.getMaxAllowedKeyLength("AES");
        LOG.debug("Max Key Size for AES : {}", maxKeySize);
        return maxKeySize;
    }

    public String encrypt(String path, byte[] bytes) throws URISyntaxException {
        LOG.debug("Encrypt {} bytes using keyring {}", bytes.length, path);
        String data = Base64.getEncoder().encodeToString(bytes);
        String result = vaultOperations.opsForTransit().encrypt(path, data);
        LOG.debug("Wrote encrypted string length: {}", result.length());
        return result;
    }

    public byte[] decrypt(String path, String data) {
        LOG.debug("Decrypt {} characters using keyring {}", data.length(), path);
        String result = vaultOperations.opsForTransit().decrypt(path, data);
        byte[] bytes = Base64.getDecoder().decode(result);
        LOG.debug("Wrote encrypted bytes length: {}", bytes.length);
        return bytes;
    }

    public void createKey(String keyName, boolean convergentEncryption, boolean exportable){
        // config parameters described here https://www.vaultproject.io/api/secret/transit
        String keyType = "rsa-4096";
        VaultTransitKeyCreationRequest keyRequest = VaultTransitKeyCreationRequest.builder()
                                                    .convergentEncryption(convergentEncryption)
                                                    .exportable(exportable)
                                                    .type(keyType)
                                                    .build();
        LOG.debug("CreateKey: {} of type: {}", keyName, keyType);
        vaultOperations.opsForTransit().createKey(keyName, keyRequest);
        LOG.debug("Key: {} created", keyName);

    }

    public String exportKey(String keyName){
        RawTransitKey key = vaultOperations.opsForTransit().exportKey(keyName, TransitKeyType.ENCRYPTION_KEY);
        String keyString = key.getName() + "\n";
        for(String name :  key.getKeys().keySet()){
            String value = key.getKeys().get(name);
            keyString += "   " + name + ": " + value +  "\n";
        }
        LOG.debug("Key: {} exported", keyName);
        return keyString;
    }

    public VaultTransitKey getKey(String keyName) {
        VaultTransitKey key = vaultOperations.opsForTransit().getKey(keyName);
        return key;
    }

}
