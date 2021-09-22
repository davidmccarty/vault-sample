package com.garage.crypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Iterator;

import com.amazonaws.services.kms.model.NotFoundException;
import com.garage.crypt.BCCrypt.BCConfig;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

public abstract class Utils {

	private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

	public static PGPPublicKey getPublicKey(BCConfig config)
			throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, PGPException {
		switch (config.getFileType()) {
			case "PKCS12":
				return getPublicKeyPKCS12(config.getPkcs12FilePath(), config.getPkcs12KeyPassword(), config.getPkcs12KeyAlias(), config.getPkcs12KeyPassword());
			case "PGP":
				return getPublicKeyPGP(config.getPgpPublicFilePath());
			default:
				String msg = "Unsupported key file format:" + config.getFileType() + ". Must be PGP or PKCS12";
				throw new IllegalStateException(msg);
		}
	}

	private static PGPPublicKey getPublicKeyPKCS12(String filePath, String password, String keyAlias, String keyPassword)
			throws IOException, PGPException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {

		File file = ResourceUtils.getFile(filePath);
		FileInputStream is = new FileInputStream(file);
    	KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
    	keystore.load(is, password.toCharArray());
    	Key key = keystore.getKey(keyAlias, keyPassword.toCharArray());
		is.close();

		PGPPublicKey pgpPublicKey = null;
		if (key instanceof PrivateKey) {
      		// Create public key from certificaye
			Certificate cert = keystore.getCertificate(keyAlias);
      		PublicKey publicKey = cert.getPublicKey();

			// Convert public key to PGP format
			JcaPGPKeyConverter cvt = new JcaPGPKeyConverter();
			pgpPublicKey = cvt.getPGPPublicKey(PGPPublicKey.RSA_GENERAL, publicKey, new Date(0));
		}
		if(pgpPublicKey == null){
			String msg = "No public key available in keystore:" + filePath + " at alias:" + keyAlias;
			throw new NotFoundException(msg);
		}
		LOG.debug("Resolved the public PGP key with id: " + pgpPublicKey.getKeyID() + " from PCKS12 file ");
		return pgpPublicKey;

	}

	private static PGPPublicKey getPublicKeyPGP(String filePath) throws IOException, PGPException {

		File file = ResourceUtils.getFile(filePath);
		InputStream keyInputStrem = new FileInputStream(file);

		InputStream in = PGPUtil.getDecoderStream(keyInputStrem);
		PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(in, new JcaKeyFingerprintCalculator());
		PGPPublicKey key = null;

		Iterator<PGPPublicKeyRing> rIt = pgpPub.getKeyRings();
		while (key == null && rIt.hasNext()) {
			PGPPublicKeyRing kRing = (PGPPublicKeyRing) rIt.next();
			Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
			while (key == null && kIt.hasNext()) {
				PGPPublicKey k = (PGPPublicKey) kIt.next();
				if (k.isEncryptionKey()) {
					key = k;
				}
			}
		}

		if (key == null) {
			LOG.error("Can't find encryption key in key ring: {}", filePath);
			throw new IllegalArgumentException("Can't find encryption key in key ring: " + filePath);
		}
		in.close();
		keyInputStrem.close();
		LOG.debug("Resolved the public PGP key with id: " + key.getKeyID() + " from PGP file");
		return key;
	}

	public static PGPSecretKey getSecretKey(BCConfig config)
			throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, PGPException {
		switch (config.getFileType()) {
			case "PKCS12":
				return getSecretKeyPCKS12(config.getPkcs12FilePath(), config.getPkcs12KeyPassword(), config.getPkcs12KeyAlias(), config.getPkcs12KeyPassword());
			case "PGP":
				return getSecretKeyPGP(config.getPgpSecretFilePath());
			default:
				String msg = "Unsupported key file format:" + config.getFileType() + ". Must be PGP or PKCS12";
				throw new IllegalStateException(msg);
		}
	}

	private static PGPSecretKey getSecretKeyPCKS12(String filePath, String password, String keyAlias, String keyPassword)
			throws IOException, PGPException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {

		File file = ResourceUtils.getFile(filePath);
		FileInputStream is = new FileInputStream(file);
    	KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
    	keystore.load(is, password.toCharArray());
    	Key key = keystore.getKey(keyAlias, keyPassword.toCharArray());
		is.close();

		PGPSecretKey pgpSecretKey = null;
		if (key instanceof PrivateKey) {
			PrivateKey privateKey = (PrivateKey)key;
      		// Create public key from certificaye
			Certificate cert = keystore.getCertificate(keyAlias);
      		PublicKey publicKey = cert.getPublicKey();

			// Convert public key to PGP format
			JcaPGPKeyConverter cvt = new JcaPGPKeyConverter();
			PGPPublicKey pgpPublicKey = cvt.getPGPPublicKey(PGPPublicKey.RSA_GENERAL, publicKey, new Date(0));
			PGPPrivateKey pgpPrivateKey = cvt.getPGPPrivateKey(pgpPublicKey, privateKey);
    		pgpSecretKey = new PGPSecretKey(pgpPrivateKey, pgpPublicKey, null, true, null);
		}
		if(pgpSecretKey == null){
			String msg = "No private key available in keystore:" + filePath + " at alias:" + keyAlias;
			throw new NotFoundException(msg);
		}
		LOG.debug("Resolved the secret PGP key with id: " + pgpSecretKey.getKeyID() + " from PCKS12 file");
		return pgpSecretKey;

	}

	public static PGPSecretKey getSecretKeyPGP(String filePath) throws IOException, PGPException {
		File file = ResourceUtils.getFile(filePath);
		InputStream in = new FileInputStream(file);
		in = PGPUtil.getDecoderStream(in);
		PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(in, new JcaKeyFingerprintCalculator());

		PGPSecretKey key = null;

		Iterator<PGPSecretKeyRing> rIt = pgpSec.getKeyRings();
		while (key == null && rIt.hasNext()) {
			PGPSecretKeyRing kRing = (PGPSecretKeyRing) rIt.next();
			Iterator<PGPSecretKey> kIt = kRing.getSecretKeys();

			while (key == null && kIt.hasNext()) {
				PGPSecretKey k = (PGPSecretKey) kIt.next();
				if (k.isSigningKey()) {
					key = k;
				}
			}
		}

		if (key == null) {
			LOG.error("Can't find decryption key in key ring: {}", filePath);
			throw new IllegalArgumentException("Can't find decryption key in key ring.");
		}
		in.close();
		LOG.debug("Resolved the secret PGP key with id: " + key.getKeyID() + " from PGP file");
		return key;
	}




}
