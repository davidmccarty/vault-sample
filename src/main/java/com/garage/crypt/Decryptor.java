package com.garage.crypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import com.garage.crypt.BCCrypt.BCConfig;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Decryptor {

	private static final Logger LOG = LoggerFactory.getLogger(Decryptor.class);

	private BCConfig config;

	public Decryptor(BCConfig config) {
		super();
		this.config = config;
	}

	public byte[] decrypt(String data)
			throws NoSuchProviderException, SignatureException, IOException, PGPException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		Security.addProvider(new BouncyCastleProvider());

		// Read private PGP key from asc file
		PGPSecretKey decryptionKey = Utils.getSecretKey(config);

		ByteArrayInputStream encryptedTextInputStream = new ByteArrayInputStream(data.getBytes());
		EncryptedInputStream encryptedInputStream = new EncryptedInputStream(encryptedTextInputStream, decryptionKey, config.getPgpPassphrase().toCharArray(), config.getProvider());

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] b = new byte[4];

		while ((nRead = encryptedInputStream.read(b, 0, b.length)) != -1) {
  			buffer.write(b, 0, nRead);
		}

	    buffer.flush();
	    byte[] bytes = buffer.toByteArray();
		encryptedInputStream.close();
		LOG.debug("Wrote encrypted bytes length: {}", bytes.length);
		return bytes;
	}

}
