package com.garage.spring;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;

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

	@PostMapping("/bouncycastle/encrypt-string")
	@ApiOperation("Encrypt string using bouncy castle")
	public String bcEncryptString(
		@ApiParam(value = "data string to encrypt", required = true, example = "hello world") @RequestParam String data)
			throws URISyntaxException, IOException, NoSuchProviderException, SignatureException, NoSuchAlgorithmException, PGPException {
		System.out.println("###   BOUNCYCASTLE-ENCRYPT-STRING   ###");
		byte[] bytes = data.getBytes();
		String response = bouncyCastleService.encrypt(bytes);
		return response;
	}

	@PostMapping("/bouncycastle/decrypt-string")
	@ApiOperation("Decrypt string using bouncy castle")
	public String bcDecryptString(
		@ApiParam(value = "data string to encrypt", required = true, type = "textarea",
				example = "-----BEGIN PGP MESSAGE-----" + "\n" +
				"Version: BCPG v1.57" + "\n" +
				"\n" +
				"hQIMAw8gcUepTD1fAQ/8CpVPp1NUgipNkl/12tpWV1lso5tzyYwJZXtr3kX6WBej" + "\n" +
				"EPoHhvNuLhZU5XOquGK1qGgdtSH8PYmJrgjdh12anfeSo4yx0ADDb8LsLkeC6cWm" + "\n" +
				"nIAdRmW5tOfpKoWKMzm4zq5QLoLgoWo5C/xUAtWltiLpnACHrW6jDZNibwze1VHT" + "\n" +
				"XgHA72fRGHDV2U0PVUFybxR6UoHY62l7E6cJe7nWoRbTqe77a4miaRW3aHQy50b2" + "\n" +
				"EmmhGCXXjBIWr5w7/rBJzOXSNGWBv0qSNj4YL1SXwMid/skvfb/b8xPH+jGYFsB9" + "\n" +
				"mTU38faJ5jJoIhSizqU5XgtUb/YYMD7hifWVJkbMAYvDzgubZHGNVFt+pg0OAJy5" + "\n" +
				"PaNvolzefUUshhw+ebetq35cwz2pMxCAfEwAQkjxK8jJ4CUd2K3BejMQg1SedUkn" + "\n" +
				"MdeTcE8NqMRuslZny4s9eDSOEhHrdW+Q0a+DsbsK6IDBTxON9X5XnuKVQ8U/ngbZ" + "\n" +
				"lp3ircvIfVfy1dX24eesU0JQJIoA1wTUabBEVXtN7bsIX53DINc1tqrB1+W6xDvo" + "\n" +
				"k4jl6Y93BW1jMZuEx3OsdqHriKA4RpuQyWnGQ9i8ZCsKrshFYq7+sHuY4CLaEETg" + "\n" +
				"WdthxOK9g11UkIokqm/WnAI/p+aWL18bs38BJlBSFUqzbck+x8v7YU0JRKc/hczJ" + "\n" +
				"KgtAh8VGc8AcoeAqJb8HfKz1R4mEb7Dxb08eFOO+gAsVY6kmqidWVXodQw==" + "\n" +
				"=oQvo" + "\n" +
				"-----END PGP MESSAGE-----") @RequestParam String data)
			throws NoSuchProviderException, SignatureException, IOException, PGPException {
		System.out.println("###   BOUNCYCASTLE-DECRYPT-STRING   ###");
		String response = bouncyCastleService.decrypt(data);
		return response;
	}

	@GetMapping("/vault/encryptLocal")
	public String encryptLocal() throws URISyntaxException, NoSuchAlgorithmException {
		return "Not yet implemented";
	}



}
