package com.garage.vault;

import java.net.URISyntaxException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultOperations;

@Service
public class VaultTransitService {

    @Autowired
    private VaultOperations vaultOperations;

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

}
