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
    private static final String SIGNING_PUBLIC_KEY_FILE = "garage-public-key.asc";
    private static final String SIGNING_PRIVATE_KEY_FILE = "garage-private-key.asc";
    private static final String SIGNING_PASS_PHRASE = "passw0rd";

    public String encrypt(String data) throws NoSuchProviderException, SignatureException, NoSuchAlgorithmException, IOException, PGPException {
        String pathPublicKey = new ClassPathResource(ENCRYPTION_PUBLIC_KEY_FILE).getFile().getAbsolutePath();
        String pathPrivateKey = new ClassPathResource(SIGNING_PRIVATE_KEY_FILE).getFile().getAbsolutePath();
        Encryptor encryptor = new Encryptor(pathPublicKey, pathPrivateKey,SIGNING_PASS_PHRASE.toCharArray());
        String encryptedBlock = encryptor.signAndEncrypt(data);
        return encryptedBlock;
    }

    public String decrypt(String data) throws NoSuchProviderException, SignatureException, IOException, PGPException {
        String pathPublicKey = new ClassPathResource(SIGNING_PUBLIC_KEY_FILE).getFile().getAbsolutePath();
        String pathPrivateKey = new ClassPathResource(ENCRYPTION_PRIVATE_KEY_FILE).getFile().getAbsolutePath();

        Decryptor decryptor = new Decryptor(pathPrivateKey, ENCRYPTION_PASS_PHRASE, true, pathPublicKey);
        String decryptedText = decryptor.decryptAndVerify(data);
        return decryptedText;
    }

}
