package com.garage.crypt;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;

import org.bouncycastle.openpgp.PGPException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class BouncyCastleService {

    private static final String ENCRYPTION_PUBLIC_KEY_FILE = "garage-public-key.asc";
    private static final String ENCRYPTION_PRIVATE_KEY_FILE = "garage-private-key.asc";
    private static final String ENCRYPTION_PASS_PHRASE = "passw0rd";

    public String encrypt(byte[] bytes) throws NoSuchProviderException, SignatureException, NoSuchAlgorithmException, IOException, PGPException {
        String pathPublicKey = new ClassPathResource(ENCRYPTION_PUBLIC_KEY_FILE).getFile().getAbsolutePath();
        Encryptor encryptor = new Encryptor(pathPublicKey);
        String encryptedBlock = encryptor.encrypt(bytes);
        return encryptedBlock;
    }

    public byte[] decrypt(String data) throws NoSuchProviderException, SignatureException, IOException, PGPException {
        String pathPrivateKey = new ClassPathResource(ENCRYPTION_PRIVATE_KEY_FILE).getFile().getAbsolutePath();
        Decryptor decryptor = new Decryptor(pathPrivateKey, ENCRYPTION_PASS_PHRASE);
        byte[] decryptedBytes = decryptor.decrypt(data);
        return decryptedBytes;
    }

}
