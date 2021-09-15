package com.garage.crypt;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.SignatureException;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPOnePassSignature;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.operator.PGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;


public class SignedInputStream extends InputStream {

	InputStream signedInputStream;
	PGPOnePassSignature onePassSignature;

	public SignedInputStream(InputStream inputStream, PGPOnePassSignatureList onePassSignatureList,
			PGPPublicKey verificationKey, String provider) throws NoSuchProviderException, PGPException {
		this.signedInputStream = inputStream;

		for (int index = 0; onePassSignature == null && index < onePassSignatureList.size(); index++) {
			PGPOnePassSignature tempOnePassSignature = onePassSignatureList.get(index);
			if (tempOnePassSignature.getKeyID() == verificationKey.getKeyID()) {
				onePassSignature = tempOnePassSignature;
			}
		}

		if (onePassSignature == null) {
			throw new IllegalArgumentException("Message not signed by verification key.");
		}
		PGPContentVerifierBuilderProvider contentVerifierBuilderProvider = new JcaPGPContentVerifierBuilderProvider();
		onePassSignature.init(contentVerifierBuilderProvider, verificationKey);
	}

	public int read() throws IOException {
		int ch = signedInputStream.read();
		if (ch >= 0) {
			try {
				onePassSignature.update((byte) ch);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return ch;
	}

	public boolean verify(PGPSignatureList signatureList)
			throws IllegalArgumentException, PGPException, SignatureException {

		PGPSignature signature = null;
		for (int index = 0; signature == null && index < signatureList.size(); index++) {
			PGPSignature tempSignature = signatureList.get(index);
			if (tempSignature.getKeyID() == onePassSignature.getKeyID()) {
				signature = tempSignature;
			}
		}

		if (signature == null) {
			throw new IllegalArgumentException("Signature not found for verification key.");
		}

		return onePassSignature.verify(signature);
	}

}
