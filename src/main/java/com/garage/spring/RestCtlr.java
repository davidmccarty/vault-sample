package com.garage.spring;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.garage.cos.MinioS3Client;
import com.garage.crypt.BouncyCastleService;
import com.garage.data.KeyValuePair;
import com.garage.vault.VaultSecretsService;
import com.garage.vault.VaultTransitService;

import org.bouncycastle.openpgp.PGPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;


@RestController
@Api(description = "Sample app for vault and cos")
public class RestCtlr {

	@Autowired
	VaultSecretsService vaultSecretsService;
	@Autowired
	VaultTransitService vaultTransitService;
	@Autowired
	MinioS3Client minioS3Client;
	@Autowired
	BouncyCastleService bouncyCastleService;

	@GetMapping("/hello")
	@ApiOperation("Check server is alive")
	public String index() {
		System.out.println("###   HELLO WORLD   ###");
		return "Greetings from Spring Boot!";
	}

	@GetMapping("/java/checkEncryptionPolicy")
	@ApiOperation("Check java crypto policy is set to unlimited")
	public String checkEncryptionPolicy() throws NoSuchAlgorithmException {
		System.out.println("###   CHECK JAVA CRYPTO POLICY   ###");
		int maxKeyLength = vaultTransitService.checkEncryptionPolich();
		if(maxKeyLength < 2147483647){
			return "ERROR: java only supports maxKeyLength " + maxKeyLength;
		} else {
			return "OK: java supports maxKeyLength " + maxKeyLength;
		}
	}

	@PostMapping("/vault/put-kv-secret")
	@ApiOperation("Put key/value secret to path")
	public String putSecret(
			@ApiParam(value = "vault path for secret", required = true, example = "secret/my-credential") @RequestParam String path,
			@ApiParam(value = "secret key", required = true, example = "admin") @RequestParam String key,
			@ApiParam(value = "secret value", required = true, example = "password") @RequestParam String value)
			throws URISyntaxException {
		System.out.println("###   PUT-KV-SECRET   ###");
		KeyValuePair secret = new KeyValuePair(key, value);
		vaultSecretsService.putSecret(path, (Object) secret);
		return "Put secret successful";
	}

	@PostMapping("/vault/get-kv-secret")
	@ApiOperation("Get key/value secret from path")
	public String getSecret(
			@ApiParam(value = "vault path for secret", required = true, example = "secret/my-credential") @RequestParam String path)
			throws URISyntaxException {
		System.out.println("###   GET-KV-SECRET   ###");
		KeyValuePair secret = vaultSecretsService.getSecret(path, KeyValuePair.class);
		return "Got secret ... " + secret;
	}

	@PostMapping("/vault/put-file-secret")
	@ApiOperation("Put key/file secret to path")
	public String putFileSecret(){
		return "Not yet implemented";
	}

	@PostMapping("/vault/get-file-secret")
	@ApiOperation("Get key/file secret from path")
	public String getFileSecret() {
				return "Not yet implemented";
	}

	@PostMapping("/cos/upload-string")
	@ApiOperation("Upload string to cos bucket with key")
	public String upload(
		@ApiParam(value = "cos bucket name (clear-test or encrypted)", required = true, example = "clear-text") @RequestParam String bucket,
		@ApiParam(value = "cos bucket key", required = true, example = "my-key") @RequestParam String key,
		@ApiParam(value = "data string to store", required = true, example = "hello world") @RequestParam String data)
			throws URISyntaxException, IOException {
		System.out.println("###   COS-UPLOAD   ###");
		String result = minioS3Client.uploadString(bucket, key, data);
		return result;
	}

	@PostMapping("/cos/download-string")
	@ApiOperation("Download string from cos bucket at key")
	public String download(
		@ApiParam(value = "cos bucket name (clear-test or encrypted)", required = true, example = "clear-text") @RequestParam String bucket,
		@ApiParam(value = "cos bucket key", required = true, example = "my-key") @RequestParam String key)
			throws IOException  {
		String data = minioS3Client.downloadString(bucket, key);
		return data;
	}

	@PostMapping("/vault/encrypt-string-transit")
	@ApiOperation("Encrypt string with vault transit keyring")
	public String encryptStringTransit(
		@ApiParam(value = "data string to encrypt", required = true, example = "hello world") @RequestParam String data,
		@ApiParam(value = "transit keyring path", required = true, example = "vault-sample") @RequestParam String path)
	 		throws URISyntaxException {
		System.out.println("###   ENCRYPT-STRING-TRANSIT  ###");
		byte[] bytes = data.getBytes();
		String result = vaultTransitService.encrypt(path, bytes);
		return result;
	}

	@PostMapping("/vault/decrypt-string-transit")
	@ApiOperation("Decrypt string with vault transit keyring")
	public String decryptStringTransit(
		@ApiParam(value = "data string to encrypt", required = true,
			example = "vault:v1:9Q7KWk1/W9StN/92LE5fRY8tyRP2OVtHkFXtbQD6HalbgOVdik+n0CSlDjA=") @RequestParam String data,
		@ApiParam(value = "transit keyring path", required = true, example = "vault-sample") @RequestParam String path)
		 	throws URISyntaxException {
		System.out.println("###   DECRYPT-STRING-TRANSIT   ###");
		String result = new String(vaultTransitService.decrypt(path, data));
		return result;
	}

	@GetMapping("/vault/encryptLocal")
	public String encryptLocal() throws URISyntaxException, NoSuchAlgorithmException {
		System.out.println("###   ENCRYPT LOCAL   ###");
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
		byte[] bytes = formatter.format(calendar.getTime()).getBytes();
		String path = "dev-ring";
		String result = vaultTransitService.encryptLocal(path, bytes);
		return "Encryption result = " + result;
	}

	@PostMapping("/bouncycastle/encrypt")
	@ApiOperation("Encrypt string using bouncy castle")
	public String bcEncrypt(String data) throws URISyntaxException, IOException, NoSuchProviderException, SignatureException, NoSuchAlgorithmException, PGPException {
		System.out.println("###   BOUNCYCASTLE-ENCRYPT   ###");
		String response = bouncyCastleService.encrypt(data);
		return response;
	}

	@PostMapping("/bouncycastle/decrypt")
	@ApiOperation("Decrypt string using bouncy castle")
	public String bcDecrypt(String data) throws NoSuchProviderException, SignatureException, IOException, PGPException {
		System.out.println("###   BOUNCYCASTLE-DECRYPT   ###");
		String response = bouncyCastleService.decrypt(data);
		return response;
	}

}
