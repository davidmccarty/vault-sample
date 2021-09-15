package com.garage.crypt;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;

public class EncryptedOutputStream extends OutputStream {

	static final int BUFFER_SIZE = 1 << 16; // should always be power of 2

	PGPEncryptedDataGenerator encryptedDataGenerator;
	OutputStream encryptedOutputStream;

	public EncryptedOutputStream(boolean withIntegrityCheck, PGPPublicKey[] publicKeys, OutputStream outputStream,
			String provider) throws IOException, PGPException, NoSuchProviderException {
		// Initialize encrypted data generator
		encryptedDataGenerator = new PGPEncryptedDataGenerator(new JcePGPDataEncryptorBuilder(SymmetricKeyAlgorithmTags.CAST5)
								.setWithIntegrityPacket(withIntegrityCheck)
								.setSecureRandom(new SecureRandom())
								.setProvider(provider));

		for (int i = 0; i < publicKeys.length; i++) {
			encryptedDataGenerator.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(publicKeys[i]));
		}

		encryptedOutputStream = encryptedDataGenerator.open(outputStream, new byte[BUFFER_SIZE]);

	}

	public EncryptedOutputStream(boolean withIntegrityCheck, PGPPublicKey publicKey, OutputStream outputStream,
			String provider) throws IOException, PGPException, NoSuchProviderException {
		this(withIntegrityCheck, new PGPPublicKey[] { publicKey }, outputStream, provider);
	}

	public void write(int b) throws IOException {
		encryptedOutputStream.write(b);
	}

	public void close() throws IOException {
		if (encryptedOutputStream != null) {
			encryptedOutputStream.flush();
			encryptedOutputStream.close();
			encryptedOutputStream = null;
		}

		if (encryptedDataGenerator != null) {
			encryptedDataGenerator.close();
			encryptedDataGenerator = null;
		}
	}

}
