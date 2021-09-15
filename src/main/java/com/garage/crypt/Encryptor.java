package com.garage.crypt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import org.bouncycastle.openpgp.PGPSecretKey;

public class Encryptor {

	private String encryptionPublicKey;
	private String signingPrivateKey;
	private char[] signingKeyPassphrase;

	public Encryptor(String encryptionPublicKey, String signingPrivateKey, char[] signingKeyPassphrase) {
		super();
		this.encryptionPublicKey = encryptionPublicKey;
		this.signingPrivateKey = signingPrivateKey;
		this.signingKeyPassphrase = signingKeyPassphrase;
	}

	// public String encrypt(String text) throws IOException, PGPException, NoSuchProviderException,
	// 					SignatureException, NoSuchAlgorithmException {
	// 	Security.addProvider(new BouncyCastleProvider());

	// 	PGPSecretKey signingKey = Utils.findSecretKey(new FileInputStream(new File(signingPrivateKey)));
	// 	PGPPublicKey encryptionKey = Utils.readPublicKey(encryptionPublicKey);

	// 	String result = encrypt(encryptionKey, text, "dummy.txt", new Date(), "BC");
	// 	return result;
	// }

	public String signAndEncrypt(String text) throws IOException, PGPException, NoSuchProviderException,
						SignatureException, NoSuchAlgorithmException {
		Security.addProvider(new BouncyCastleProvider());

		PGPSecretKey signingKey = Utils.findSecretKey(new FileInputStream(new File(signingPrivateKey)));
		PGPPublicKey encryptionKey = Utils.readPublicKey(encryptionPublicKey);

		String result = signAndEncrypt(signingKey, signingKeyPassphrase, encryptionKey, text, "dummy.txt", new Date(), "BC");
		return result;
	}

	// private String encrypt(PGPPublicKey encryptionKey,
	// 		String text, String fileName, Date lastModified, String provider)
	// 		throws NoSuchProviderException, PGPException, IOException, SignatureException, NoSuchAlgorithmException {

	// 	// This order is important.Note everything is written back to ByteArrayOS.
	// 	// SignedOS and LiteralData OS IS IMPORTANT AND THEY ARE WRITTEN BACK TO PREVIOUS STREAM

	// 	ByteArrayOutputStream encryptedSignedFileOutputStream = new ByteArrayOutputStream();

	// 	ArmoredOutputStream encryptedSignedArmoredOutputStream = new ArmoredOutputStream(
	// 			encryptedSignedFileOutputStream);

	// 	EncryptedOutputStream encryptedOutputStream = new EncryptedOutputStream( // true,
	// 			false, encryptionKey, encryptedSignedArmoredOutputStream, provider);

	// 	CompressedDataOutputStream compressedDataOutputStream = new CompressedDataOutputStream(encryptedOutputStream);

	// 	LiteralDataOutputStream literalDataOutputStream = new LiteralDataOutputStream(fileName, text.length(),
	// 			lastModified, compressedDataOutputStream);

	// 	char[] textCharArray = text.toCharArray();

	// 	for (int index = 0; index < textCharArray.length; index++) {
	// 		literalDataOutputStream.write((int) textCharArray[index]);
	// 	}

	// 	// The order in which the signed output stream and the literal data
	// 	// stream are closed is important.

	// 	literalDataOutputStream.close();

	// 	compressedDataOutputStream.close();
	// 	encryptedOutputStream.close();
	// 	encryptedSignedArmoredOutputStream.close();
	// 	encryptedSignedFileOutputStream.close();

	// 	return encryptedSignedFileOutputStream.toString();
	// }

	private String signAndEncrypt(PGPSecretKey signingKey, char[] passPhrase, PGPPublicKey encryptionKey,
			String text, String fileName, Date lastModified, String provider)
			throws NoSuchProviderException, PGPException, IOException, SignatureException, NoSuchAlgorithmException {

		// This order is important.Note everything is written back to ByteArrayOS.
		// SignedOS and LiteralData OS IS IMPORTANT AND THEY ARE WRITTEN BACK TO PREVIOUS STREAM

		ByteArrayOutputStream encryptedSignedFileOutputStream = new ByteArrayOutputStream();

		ArmoredOutputStream encryptedSignedArmoredOutputStream = new ArmoredOutputStream(
				encryptedSignedFileOutputStream);

		EncryptedOutputStream encryptedOutputStream = new EncryptedOutputStream( // true,
				false, encryptionKey, encryptedSignedArmoredOutputStream, provider);

		CompressedDataOutputStream compressedDataOutputStream = new CompressedDataOutputStream(encryptedOutputStream);

		SignedOutputStream signedOutputStream = new SignedOutputStream(signingKey, passPhrase,
				compressedDataOutputStream, provider);

		LiteralDataOutputStream literalDataOutputStream = new LiteralDataOutputStream(fileName, text.length(),
				lastModified, compressedDataOutputStream);

		char[] textCharArray = text.toCharArray();

		for (int index = 0; index < textCharArray.length; index++) {
			// Must write to both the signed and literal data output streams.
			signedOutputStream.write((int) textCharArray[index]);
			literalDataOutputStream.write((int) textCharArray[index]);
		}

		// The order in which the signed output stream and the literal data
		// stream are closed is important.

		literalDataOutputStream.close();
		signedOutputStream.close();

		compressedDataOutputStream.close();
		encryptedOutputStream.close();
		encryptedSignedArmoredOutputStream.close();
		encryptedSignedFileOutputStream.close();

		return encryptedSignedFileOutputStream.toString();
	}
}
