package com.garage.crypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.util.Date;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Encryptor {

	private static final Logger LOG = LoggerFactory.getLogger(BCCrypt.class);

	// TODO - properties file for these values
	private String encryptionPublicKey;
	private String provider = "BC";
	private String dummyFilename = "dummy.txt";

	public Encryptor(String encryptionPublicKey) {
		super();
		this.encryptionPublicKey = encryptionPublicKey;
	}

	public String encrypt(byte[] bytes) throws IOException, PGPException, NoSuchProviderException,
						SignatureException, NoSuchAlgorithmException {
		Security.addProvider(new BouncyCastleProvider());
		// Read public PGP key from asc file
		PGPPublicKey encryptionKey = Utils.readPublicKey(encryptionPublicKey);
		LOG.debug("Resolved encryption key from: {} with keyId: {}", encryptionPublicKey, encryptionKey.getKeyID());

		// Stream processing order:
		// bytes
		//	--> literalDataOutputStream
		//		--> compressedDataOutputStream
		//			--> encryptedOutputStream
		//				--> encryptedArmoredOutputStream
		//					--> encryptedFileOutputStream

		ByteArrayOutputStream encryptedFileOutputStream = new ByteArrayOutputStream();

		ArmoredOutputStream encryptedArmoredOutputStream = new ArmoredOutputStream(
				encryptedFileOutputStream);

		EncryptedOutputStream encryptedOutputStream = new EncryptedOutputStream( // true,
				false, encryptionKey, encryptedArmoredOutputStream, provider);

		CompressedDataOutputStream compressedDataOutputStream = new CompressedDataOutputStream(encryptedOutputStream);

		LiteralDataOutputStream literalDataOutputStream = new LiteralDataOutputStream(dummyFilename, bytes.length,
				new Date(), compressedDataOutputStream);

		for (int index = 0; index < bytes.length; index++) {
			literalDataOutputStream.write((int) bytes[index]);
		}

		// Close streams in order to ensure correct flushing
		literalDataOutputStream.close();
		compressedDataOutputStream.close();
		encryptedOutputStream.close();
		encryptedArmoredOutputStream.close();
		encryptedFileOutputStream.close();

		String result = encryptedFileOutputStream.toString();
		LOG.debug("Wrote encrypted slring length: {}", result.length());
		return result;
	}

}
