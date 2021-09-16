package com.garage.crypt;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
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

	public String decrypt(String data)
			throws NoSuchProviderException, SignatureException, IOException, PGPException {
		Security.addProvider(new BouncyCastleProvider());

		// Read private PGP key from asc file
		PGPSecretKey decryptionKey = Utils.findSecretKey(new FileInputStream(new File(privateKeyFilePath)));

		ByteArrayInputStream encryptedTextInputStream = new ByteArrayInputStream(data.getBytes());
		EncryptedInputStream encryptedInputStream = new EncryptedInputStream(encryptedTextInputStream, decryptionKey, passphrase.toCharArray(), provider);
		StringWriter stringWriter = new StringWriter();
		int ch;
		while ((ch = encryptedInputStream.read()) >= 0) {
			stringWriter.write(ch);
		}
		encryptedInputStream.close();
		return stringWriter.toString();
	}

}
