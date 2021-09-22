package com.garage.crypt;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.annotation.PostConstruct;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BCCrypt {

    private static final Logger LOG = LoggerFactory.getLogger(BCCrypt.class);


    @Value("${crypto.enabled}")
    private boolean enabled;
    @Value("${crypto.store.type:}")
    private String fileType;
    @Value("${crypto.store.pgp.secretkeyring.location:}")
    private String pgpSecretFilePath;
    @Value("${crypto.store.pgp.passphrase:}")
    private String pgpPassphrase;
    @Value("${crypto.store.pgp.publickeyring.location:}")
    private String pgpPublicFilePath;
    @Value("${crypto.store.pkcs12.location:}")
    private String pkcs12FilePath;
    @Value("${crypto.store.pkcs12.password:}")
    private String pkcs12Password;
    @Value("${crypto.store.pkcs12.keyAlias:}")
    private String pkcs12KeyAlias;
    @Value("${crypto.store.pkcs12.keyPassword:}")
    private String pkcs12KeyPassword;

    private BCConfig config;

    @PostConstruct
    private void init(){
        config = new BCConfig(fileType, pgpSecretFilePath, pgpPassphrase, pgpPublicFilePath, pkcs12FilePath, pkcs12Password, pkcs12KeyAlias, pkcs12KeyPassword);
    }


    public String encrypt(byte[] bytes) throws NoSuchProviderException, SignatureException, NoSuchAlgorithmException, IOException, PGPException, UnrecoverableKeyException, KeyStoreException, CertificateException {
        LOG.debug("Encrypt {} bytes", bytes.length);
        if( !enabled ){
            LOG.debug("Encrypt operation skipped because crypto.enabled=false}");
            return new String(bytes);
        }
        Encryptor encryptor = new Encryptor(config);
        String encryptedBlock = encryptor.encrypt(bytes);
        LOG.debug("Encrypted {} bytes to {} characters", bytes.length, encryptedBlock.length());
        return encryptedBlock;
    }

    public byte[] decrypt(String data) throws NoSuchProviderException, SignatureException, IOException, PGPException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        LOG.debug("Decrypt {} characters", data.length());
        if( !enabled ){
            LOG.debug("Decrypt operation skipped because crypto.enabled=false}");
            return data.getBytes();
        }
        Decryptor decryptor = new Decryptor(config);
        byte[] decryptedBytes = decryptor.decrypt(data);
        LOG.debug("Decrypted {} characters to {} bytes", data.length(), decryptedBytes.length);
        return decryptedBytes;
    }

    protected class BCConfig {
        private String provider = "BC";
        private String fileType;
        private String pgpSecretFilePath;
        private String pgpPassphrase;
        private String pgpPublicFilePath;
        private String pkcs12FilePath;
        private String pkcs12Password;
        private String pkcs12KeyAlias;
        private String pkcs12KeyPassword;

        public BCConfig(String fileType, String pgpSecretFilePath, String pgpPassphrase,
                String pgpPublicFilePath, String pkcs12FilePath, String pkcs12Password, String pkcs12KeyAlias,
                String pkcs12KeyPassword) {
            this.fileType = fileType;
            this.pgpSecretFilePath = pgpSecretFilePath;
            this.pgpPassphrase = pgpPassphrase;
            this.pgpPublicFilePath = pgpPublicFilePath;
            this.pkcs12FilePath = pkcs12FilePath;
            this.pkcs12Password = pkcs12Password;
            this.pkcs12KeyAlias = pkcs12KeyAlias;
            this.pkcs12KeyPassword = pkcs12KeyPassword;
        }

        public String getProvider() {
            return provider;
        }

        public String getFileType() {
            return fileType;
        }

        public String getPgpSecretFilePath() {
            return pgpSecretFilePath;
        }

        public String getPgpPassphrase() {
            return pgpPassphrase;
        }

        public String getPgpPublicFilePath() {
            return pgpPublicFilePath;
        }

        public String getPkcs12FilePath() {
            return pkcs12FilePath;
        }


        public String getPkcs12Password() {
            return pkcs12Password;
        }

        public String getPkcs12KeyAlias() {
            return pkcs12KeyAlias;
        }

        public String getPkcs12KeyPassword() {
            return pkcs12KeyPassword;
        }

    }

}
