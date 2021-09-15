package com.garage.crypt;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;

import org.bouncycastle.openpgp.PGPException;

public class Main {

	private static final String ENCRYPTION_PUBLIC_KEY_FILE = "encryptionpublickey.asc";
	private static final String ENCRYPTION_PRIVATE_KEY_FILE = "encryptionprivatekey.asc";
	private static final String ENCRYPTION_PASS_PHRASE = "TomHenry";
	private static final String SIGNING_PUBLIC_KEY_FILE = "signingpublickey.asc";
	private static final String SIGNING_PRIVATE_KEY_FILE = "signingprivatekey.asc";
	private static final String SIGNING_PASS_PHRASE = "MarkSmith";

	public static void main(String [] args) throws NoSuchProviderException, SignatureException, NoSuchAlgorithmException, IOException, PGPException {

        String dataToBeEncrypted = "123456789";
        Encryptor encryptor = new Encryptor(ENCRYPTION_PUBLIC_KEY_FILE,
        		SIGNING_PRIVATE_KEY_FILE,
        		SIGNING_PASS_PHRASE.toCharArray());
        String encryptedBlock = encryptor.signAndEncrypt(dataToBeEncrypted);

        //Starting decryption
        Decryptor bcpgpDecryptor = new Decryptor(ENCRYPTION_PRIVATE_KEY_FILE,
        		ENCRYPTION_PASS_PHRASE, true, SIGNING_PUBLIC_KEY_FILE);

        String decryptedText = bcpgpDecryptor.decryptAndVerify(encryptedBlock);
        System.out.println("Decrypted text: " + decryptedText);

	}
}
