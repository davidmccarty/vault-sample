package com.garage.crypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Decryptor {

	private static final Logger LOG = LoggerFactory.getLogger(Decryptor.class);

	/// TODO - move to properties
	private String privateKeyFilePath;
	private String passphrase;
	private String provider = "BC";

	public Decryptor(String privateKeyFilePath, String passphrase) {
		super();
		this.privateKeyFilePath = privateKeyFilePath;
		this.passphrase = passphrase;
	}

	public byte[] decrypt(String data)
			throws NoSuchProviderException, SignatureException, IOException, PGPException {
		Security.addProvider(new BouncyCastleProvider());

		// Read private PGP key from asc file
		PGPSecretKey decryptionKey = Utils.findSecretKey(privateKeyFilePath);
		LOG.debug("Resolved decryption key from: {} with keyId: {}", privateKeyFilePath, decryptionKey.getKeyID());

		ByteArrayInputStream encryptedTextInputStream = new ByteArrayInputStream(data.getBytes());
		EncryptedInputStream encryptedInputStream = new EncryptedInputStream(encryptedTextInputStream, decryptionKey, passphrase.toCharArray(), provider);

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
