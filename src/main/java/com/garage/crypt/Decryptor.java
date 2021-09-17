package com.garage.crypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPSecretKey;

public class Decryptor {

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
		PGPSecretKey decryptionKey = Utils.findSecretKey(new FileInputStream(new File(privateKeyFilePath)));

		ByteArrayInputStream encryptedTextInputStream = new ByteArrayInputStream(data.getBytes());
		EncryptedInputStream encryptedInputStream = new EncryptedInputStream(encryptedTextInputStream, decryptionKey, passphrase.toCharArray(), provider);

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] b = new byte[16384];

		while ((nRead = encryptedInputStream.read(b, 0, b.length)) != -1) {
  			buffer.write(b, 0, nRead);
		}

	    buffer.flush();
	    byte[] bytes = buffer.toByteArray();
		encryptedInputStream.close();

		System.out.println("Decrypt: decrypted \n " +
            (bytes.length < 200 ? new String(bytes) : new String(bytes).substring(0, 200) + " ..."));
		return bytes;
	}

}
