package com.garage.crypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Utils {

	private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

	public static PGPPublicKey readPublicKey(String filePath) throws IOException, PGPException {

		InputStream keyInputStrem = new FileInputStream(new File(filePath));

		InputStream in = PGPUtil.getDecoderStream(keyInputStrem);
		PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(in, new JcaKeyFingerprintCalculator());
		PGPPublicKey key = null;

		Iterator<PGPPublicKeyRing> rIt = pgpPub.getKeyRings();
		while (key == null && rIt.hasNext()) {
			PGPPublicKeyRing kRing = (PGPPublicKeyRing) rIt.next();
			Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
			while (key == null && kIt.hasNext()) {
				PGPPublicKey k = (PGPPublicKey) kIt.next();
				if (k.isEncryptionKey()) {
					key = k;
				}
			}
		}

		if (key == null) {
			LOG.error("Can't find encryption key in key ring: {}", filePath);
			throw new IllegalArgumentException("Can't find encryption key in key ring: " + filePath);
		}
		in.close();
		keyInputStrem.close();

		return key;
	}

	// TODO - change this to get private key from secret key
	public static PGPSecretKey findSecretKey(String filePath) throws IOException, PGPException {
		InputStream in = new FileInputStream(new File(filePath));
		in = PGPUtil.getDecoderStream(in);
		PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(in, new JcaKeyFingerprintCalculator());

		PGPSecretKey key = null;

		Iterator<PGPSecretKeyRing> rIt = pgpSec.getKeyRings();
		while (key == null && rIt.hasNext()) {
			PGPSecretKeyRing kRing = (PGPSecretKeyRing) rIt.next();
			Iterator<PGPSecretKey> kIt = kRing.getSecretKeys();

			while (key == null && kIt.hasNext()) {
				PGPSecretKey k = (PGPSecretKey) kIt.next();
				if (k.isSigningKey()) {
					key = k;
				}
			}
		}

		if (key == null) {
			LOG.error("Can't find decryption key in key ring: {}", filePath);
			throw new IllegalArgumentException("Can't find decryption key in key ring.");
		}
		in.close();
		return key;
	}
}
