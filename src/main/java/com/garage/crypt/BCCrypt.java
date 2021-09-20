package com.garage.crypt;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class BCCrypt {

    private static final Logger LOG = LoggerFactory.getLogger(BCCrypt.class);

    // TODO - config this
    private static final String ENCRYPTION_PUBLIC_KEY_FILE = "garage-public-key.asc";
    private static final String ENCRYPTION_PRIVATE_KEY_FILE = "garage-private-key.asc";
    private static final String ENCRYPTION_PASS_PHRASE = "passw0rd";

    public String encrypt(byte[] bytes) throws NoSuchProviderException, SignatureException, NoSuchAlgorithmException, IOException, PGPException {
        LOG.debug("Encrypt {} bytes", bytes.length);
        String pathPublicKey = new ClassPathResource(ENCRYPTION_PUBLIC_KEY_FILE).getFile().getAbsolutePath();
        Encryptor encryptor = new Encryptor(pathPublicKey);
        String encryptedBlock = encryptor.encrypt(bytes);
        LOG.debug("Encrypted {} bytes to {} characters", bytes.length, encryptedBlock.length());
        return encryptedBlock;
    }

    public byte[] decrypt(String data) throws NoSuchProviderException, SignatureException, IOException, PGPException {
        LOG.debug("Decrypt {} characters", data.length());
        String pathPrivateKey = new ClassPathResource(ENCRYPTION_PRIVATE_KEY_FILE).getFile().getAbsolutePath();
        Decryptor decryptor = new Decryptor(pathPrivateKey, ENCRYPTION_PASS_PHRASE);
        byte[] decryptedBytes = decryptor.decrypt(data);
        LOG.debug("Decrypted {} characters to {} bytes", data.length(), decryptedBytes.length);
        return decryptedBytes;
    }

}
