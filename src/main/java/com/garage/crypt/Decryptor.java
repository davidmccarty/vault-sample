package com.garage.crypt;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;

public class Decryptor {

	private String privateKeyFilePath;
	private String password;
	private boolean isSigned;
	private String signingPublicKeyFilePath;



	public Decryptor(String privateKeyFilePath, String password, boolean isSigned,
			String signingPublicKeyFilePath) {
		super();
		this.privateKeyFilePath = privateKeyFilePath;
		this.password = password;
		this.isSigned = isSigned;
		this.signingPublicKeyFilePath = signingPublicKeyFilePath;
	}

	public boolean isSigned() {
		return isSigned;
	}

	public void setSigned(boolean isSigned) {
		this.isSigned = isSigned;
	}

	public String getSigningPublicKeyFilePath() {
		return signingPublicKeyFilePath;
	}

	public void setSigningPublicKeyFilePath(String signingPublicKeyFilePath) {
		this.signingPublicKeyFilePath = signingPublicKeyFilePath;
	}

	public String getPrivateKeyFilePath() {
		return privateKeyFilePath;
	}

	public void setPrivateKeyFilePath(String privateKeyFilePath) {
		this.privateKeyFilePath = privateKeyFilePath;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String decryptAndVerify(String encryptedAndSignedText)
			throws NoSuchProviderException, SignatureException, IOException, PGPException {
		Security.addProvider(new BouncyCastleProvider());

		PGPSecretKey decryptionKey = Utils.findSecretKey(new FileInputStream(new File(privateKeyFilePath)));
		PGPPublicKey signingPublicKey = Utils.readPublicKey(signingPublicKeyFilePath);

		return decryptAndVerify(encryptedAndSignedText, decryptionKey, password.toCharArray(), signingPublicKey, "BC");
	}

	private String decryptAndVerify(String encryptedAndSignedText, PGPSecretKey decryptionKey, char[] passPhrase,
			PGPPublicKey signingKey, String provider)
			throws NoSuchProviderException, IOException, PGPException, SignatureException {
		ByteArrayInputStream encryptedAndSignedTextInputStream = new ByteArrayInputStream(
				encryptedAndSignedText.getBytes());

		EncryptedInputStream encryptedInputStream = new EncryptedInputStream(encryptedAndSignedTextInputStream,
				decryptionKey, passPhrase, provider);

		InputStream inputStream;
		SignedInputStream signedInputStream = null;

		if (encryptedInputStream.getOnePassSignatureList() != null) {
			signedInputStream = new SignedInputStream(encryptedInputStream,
					encryptedInputStream.getOnePassSignatureList(), signingKey, provider);
			inputStream = signedInputStream;
		} else {
			inputStream = encryptedInputStream;
		}

		StringWriter stringWriter = new StringWriter();
		int ch;

		while ((ch = inputStream.read()) >= 0) {
			stringWriter.write(ch);
		}
		if (encryptedInputStream.getOnePassSignatureList() != null) {
			signedInputStream.close();
		}

		encryptedInputStream.close();
		if (encryptedInputStream.getOnePassSignatureList() != null) {
			if (signedInputStream.verify(encryptedInputStream.getSignatureList())) {
			} else {
				throw new SignatureException("Invalid signature");
			}
		}
		return stringWriter.toString();
	}
}
