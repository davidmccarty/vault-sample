package com.garage.vault;

import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Base64;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.RawTransitKey;
import org.springframework.vault.support.TransitKeyType;
import org.springframework.vault.support.VaultTransitKey;
import org.springframework.vault.support.VaultTransitKeyCreationRequest;

@Service
public class VaultTransitService {

    @Autowired
    private VaultOperations vaultOperations;

    @PostConstruct
    public void setProperty() {
        Security.setProperty("crypto.policy", "unlimited");
    }

    public String checkEncryptionPolich() throws NoSuchAlgorithmException{
        int maxKeySize = javax.crypto.Cipher.getMaxAllowedKeyLength("AES");
        System.out.println("Max Key Size for AES : " + maxKeySize);
        return Integer.toString(maxKeySize);
    }

    public String encrypt(String path, byte[] bytes) throws URISyntaxException {
        System.out.println("Encrypt: bytes = " + new String(bytes));
        String data = Base64.getEncoder().encodeToString(bytes);
        System.out.println("Encrypt: base64 = " + data);
        String result = vaultOperations.opsForTransit().encrypt(path, data);
        System.out.println("Encrypt: encrypted = " + result);
        return result;
    }

    public byte[] decrypt(String path, String data) {
        System.out.println("Decrypt: encrypted = " + data);
        String result = vaultOperations.opsForTransit().decrypt(path, data);
        System.out.println("Decrypt: decrypted = " + result);
        byte[] bytes = Base64.getDecoder().decode(result);
        System.out.println("Decrypt: decoded = " + new String(bytes));
        return bytes;
    }

    public String encryptLocal(String path, byte[] bytes) throws URISyntaxException {

        return null;
    }

    public byte[] decryptLocal(String path, byte[] bytes) throws URISyntaxException {
        return null;
    }

    public void createKey(String keyName){
        // config parameters described here https://www.vaultproject.io/api/secret/transit
        String keyType = "rsa-4096";
        VaultTransitKeyCreationRequest keyRequest = VaultTransitKeyCreationRequest.builder()
                                                    .convergentEncryption(false)
                                                    .exportable(true)
                                                    .type(keyType)
                                                    .build();
        System.out.println("CreateKey: " + keyName);
        vaultOperations.opsForTransit().createKey(keyName, keyRequest);
        System.out.println("CreateKey: " + keyName + " created");

    }

    public String exportKey(String keyName){
        RawTransitKey key = vaultOperations.opsForTransit().exportKey(keyName, TransitKeyType.ENCRYPTION_KEY);
        String keyString = key.getName() + "\n";
        for(String name :  key.getKeys().keySet()){
            String value = key.getKeys().get(name);
            keyString += "   " + name + ": " + value +  "\n";
        }
        return keyString;
    }

    public String readKey(String keyName) {
        VaultTransitKey key = vaultOperations.opsForTransit().getKey(keyName);
        String keyString = key.getName() + "\n"
                            + "         latest version :" + key.getLatestVersion() + "\n"
                            + "                   type :" + key.getType() + "\n"
                            + "    min decrypt version :" + key.getMinDecryptionVersion() + "\n"
                            + "    min encrypt version :" + key.getMinEncryptionVersion() + "\n"
                            + "      isDeletionAllowed :" + key.isDeletionAllowed() + "\n"
                            + "              isDerived :" + key.isDerived() + "\n"
                            + "           isExportable :" + key.isExportable() + "\n"
                            + "     supportsDecryption :" + key.supportsDecryption() + "\n"
                            + "     supportsDerivation :" + key.supportsDerivation() + "\n"
                            + "     supportsEncryption :" + key.supportsEncryption() + "\n"
                            + "      supportsSigning() :" + key.supportsSigning() + "\n"
                            + "                   keys :" + "\n";
        for(String name :  key.getKeys().keySet()){
            keyString += "                          " + name + "\n";
        }
        return keyString;
    }

    public void rotateKey() {
    }

    public void deleteKey() {
    }

    public void rewrapData() {
    }

    public void generateDataKey() {
    }





}
