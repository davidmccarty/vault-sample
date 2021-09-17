package com.garage.crypt;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.util.Iterator;

import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPMarker;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

public class EncryptedInputStream extends InputStream {

	private InputStream decoderStream;
	private PGPPublicKeyEncryptedData publicKeyEncryptedData;
	private PGPObjectFactory objectFactory;
	private InputStream clearTextInputStream;
	private PGPOnePassSignatureList onePassSignatureList;
	private PGPSignatureList signatureList;

	public EncryptedInputStream(InputStream encryptedInputStream, PGPSecretKey decryptionKey, char[] passPhrase,
			String provider) throws IOException, PGPException, NoSuchProviderException {

		decoderStream = PGPUtil.getDecoderStream(encryptedInputStream);
		objectFactory = new PGPObjectFactory(decoderStream, new JcaKeyFingerprintCalculator());

		Object pgpObject = objectFactory.nextObject();

		if (pgpObject instanceof PGPMarker) {
			// First object may be a marker packet. If so, skip it.
			pgpObject = objectFactory.nextObject();
		}

		if (pgpObject instanceof PGPEncryptedDataList) {

			PGPEncryptedDataList encryptedDataList = (PGPEncryptedDataList) pgpObject;
			// The encrypted text may have been encrypted for multiple recipients.
			// Find the encrypted data for which we have a secret key.

			@SuppressWarnings("unchecked")
			Iterator<PGPPublicKeyEncryptedData> encryptedDataObjectsIterator = encryptedDataList.getEncryptedDataObjects();
			while (publicKeyEncryptedData == null && encryptedDataObjectsIterator.hasNext()) {
				PGPPublicKeyEncryptedData tempPublicKeyEncryptedData = (PGPPublicKeyEncryptedData) encryptedDataObjectsIterator
						.next();

				if (tempPublicKeyEncryptedData.getKeyID() == decryptionKey.getKeyID()) {
					publicKeyEncryptedData = tempPublicKeyEncryptedData;

				}
			}

			if (publicKeyEncryptedData == null) {
				throw new IllegalArgumentException("Message not encrypted to secret key.");
			}

			PBESecretKeyDecryptor secretKeyDecryptor = new JcePBESecretKeyDecryptorBuilder().build(passPhrase);
			PGPPrivateKey pgpPrivateKey = decryptionKey.extractPrivateKey(secretKeyDecryptor);

			BcPublicKeyDataDecryptorFactory publicKeyDataDecryptorFactory = new BcPublicKeyDataDecryptorFactory(
					pgpPrivateKey);
			InputStream decryptedTextInputStream = publicKeyEncryptedData.getDataStream(publicKeyDataDecryptorFactory);

			objectFactory = new PGPObjectFactory(decryptedTextInputStream, new JcaKeyFingerprintCalculator());

			pgpObject = objectFactory.nextObject();
		}

		if (pgpObject instanceof PGPCompressedData) {
			PGPCompressedData compressedData = (PGPCompressedData) pgpObject;
			objectFactory = new PGPObjectFactory(compressedData.getDataStream(), new JcaKeyFingerprintCalculator());
			pgpObject = objectFactory.nextObject();
		}


		if (pgpObject instanceof PGPLiteralData) {
			PGPLiteralData literalData = (PGPLiteralData) pgpObject;
			clearTextInputStream = literalData.getDataStream();
		}
	}

	public int read() throws IOException {
		return clearTextInputStream.read();
	}

	public int read(byte[] b, int off, int len) throws java.io.IOException {
		return clearTextInputStream.read(b, off, len);
	}

	public void close() throws IOException {

		if (onePassSignatureList != null) {
			Object pgpClearTextObject = objectFactory.nextObject();

			if (pgpClearTextObject instanceof PGPSignatureList) {
				signatureList = (PGPSignatureList) pgpClearTextObject;
			}
		}

		if (publicKeyEncryptedData != null && publicKeyEncryptedData.isIntegrityProtected()) {
			try {
				if (!publicKeyEncryptedData.verify()) {
					throw new IOException("Could not verify integrity.");
				}
			} catch (PGPException ex) {
				throw new IOException("PGP Exception: " + ex.getMessage());
			}
		}

		clearTextInputStream.close();
		decoderStream.close();
	}

	public PGPOnePassSignatureList getOnePassSignatureList() {
		return onePassSignatureList;
	}

	public PGPSignatureList getSignatureList() {
		return signatureList;
	}

}
