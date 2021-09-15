package com.garage.crypt;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.PGPV3SignatureGenerator;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

/**
 */
public class SignedOutputStream extends OutputStream {

	OutputStream outputStream;
	PGPV3SignatureGenerator signatureGenerator;

	public SignedOutputStream(PGPSecretKey secretKey, char[] passPhrase, OutputStream outputStream, String provider)
			throws IOException, NoSuchAlgorithmException, NoSuchProviderException, PGPException, SignatureException {

		this.outputStream = outputStream;
		PBESecretKeyDecryptor secretKeyDecryptor = new JcePBESecretKeyDecryptorBuilder().build(passPhrase);
		PGPPrivateKey privateKey = secretKey.extractPrivateKey(secretKeyDecryptor);

		JcaPGPContentSignerBuilder jcaPGPContentSignerBuilder = new JcaPGPContentSignerBuilder(
				secretKey.getPublicKey().getAlgorithm(), PGPUtil.SHA1).setProvider(provider);

		// Create a signature generator that encrypts using the same algorithm
		// as the secret key's public key and hashes with the SHA1 algorithm.
		signatureGenerator = new PGPV3SignatureGenerator(jcaPGPContentSignerBuilder);

		// Initial the signature generator for use on a binary document using the private key.
		signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, privateKey);
		signatureGenerator.generateOnePassVersion(false).encode(this.outputStream);

	}

	public void write(int b) throws IOException {
		try {
			signatureGenerator.update((byte) b);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("Signature exception:" + e.getMessage());
		}

	}

	public void close() throws IOException {

		try {
			signatureGenerator.generate().encode(outputStream);
		} catch (PGPException e) {
			e.printStackTrace();
			throw new IOException("PGP exception:" + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("Signature exception:" + e.getMessage());
		}
	}
}
