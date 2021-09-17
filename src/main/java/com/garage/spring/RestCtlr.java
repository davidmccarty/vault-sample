package com.garage.spring;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;

import com.garage.cos.MinioS3Client;
import com.garage.crypt.BouncyCastleService;
import com.garage.model.KeyValuePair;
import com.garage.vault.VaultSecretsService;
import com.garage.vault.VaultTransitService;

import org.bouncycastle.openpgp.PGPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
		System.out.println("\n###   HELLO-WORLD   ###");
		return "Greetings from Spring Boot!";
	}

	@GetMapping("/java/checkEncryptionPolicy")
	@ApiOperation("Check java crypto policy is set to unlimited")
	public String checkEncryptionPolicy() throws NoSuchAlgorithmException {
		System.out.println("\n###   CHECK-JAVA-CRYPTO-POLICY   ###");
		int maxKeyLength = vaultTransitService.checkEncryptionPolich();
		if(maxKeyLength < 2147483647){
			return "ERROR: java only supports maxKeyLength " + maxKeyLength;
		} else {
			return "OK: java supports maxKeyLength " + maxKeyLength;
		}
	}

	@PostMapping("/vault/kv/string-put-secret")
	@ApiOperation("Put key/value secret to path")
	public String putStringSecret(
			@ApiParam(value = "vault path for secret", required = true, example = "secret/my-credentials") @RequestParam String path,
			@ApiParam(value = "secret key", required = true, example = "admin") @RequestParam String key,
			@ApiParam(value = "secret value", required = true, example = "password") @RequestParam String value)
			throws URISyntaxException {
		System.out.println("\n###   PUT-STRING-SECRET   ###");
		KeyValuePair secret = new KeyValuePair(key, value);
		vaultSecretsService.putSecret(path, (Object) secret);
		return "Put string secret successful";
	}

	@PostMapping("/vault/kv/string-get-secret")
	@ApiOperation("Get key/value secret from path")
	public String getStringSecret(
			@ApiParam(value = "vault path for secret", required = true, example = "secret/my-credentials") @RequestParam String path)
			throws URISyntaxException {
		System.out.println("\n###   GET-STRING-SECRET   ###");
		KeyValuePair secret = vaultSecretsService.getSecret(path, KeyValuePair.class);
		return "Got string secret ... " + secret;
	}

	@PostMapping("/vault/kv/file-put-secret")
	@ApiOperation("Put key/file secret to path")
	public String putFileSecret(
			@ApiParam(value = "vault path for secret", required = true, example = "secret/my-certificates") @RequestParam String path,
			@ApiParam(value = "secret key", required = true, example = "my-private-key.asc") @RequestParam String key,
			@ApiParam(value = "file to store in secret", required = true) @RequestPart(value = "file") MultipartFile file)
			throws URISyntaxException, IOException {
		System.out.println("\n###   PUT-FILE-SECRET   ###");
		vaultSecretsService.putSecretFile(path, key, file.getBytes());
		return "Put file secret successful";
	}

	@PostMapping("/vault/file/get-secret")
	@ApiOperation("Get key/file secret from path")
	public ResponseEntity<InputStreamResource> getFileSecret(
			@ApiParam(value = "vault path for secret", required = true, example = "secret/my-certificates") @RequestParam String path,
			@ApiParam(value = "secret key", required = true, example = "my-private-key.asc") @RequestParam String key)
			throws URISyntaxException {
		System.out.println("\n###   GET-FILE-SECRET   ###");
		byte[] result = vaultSecretsService.gettSecretFile(path, key);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/octet-stream");
        headers.add("Content-Disposition", "attachment; filename=" + key);

		InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(result));
		headers.setContentLength(result.length);
		return new ResponseEntity<InputStreamResource>(inputStreamResource, headers, HttpStatus.OK);
	}

	@PostMapping("/cos/string-upload")
	@ApiOperation("Upload string to cos bucket with key")
	public String uploadString(
		@ApiParam(value = "cos bucket name (clear-test or encrypted)", required = true, example = "clear-text") @RequestParam String bucket,
		@ApiParam(value = "cos bucket key", required = true, example = "my-key") @RequestParam String key,
		@ApiParam(value = "data string to store", required = true, example = "hello world") @RequestParam String data)
			throws URISyntaxException, IOException {
		System.out.println("\n###   COS-STRING-UPLOAD   ###");
		String result = minioS3Client.uploadBytes(bucket, key, data.getBytes());
		return result;
	}

	@PostMapping("/cos/string-download")
	@ApiOperation("Download string from cos bucket at key")
	public String downloadString(
		@ApiParam(value = "cos bucket name (clear-test or encrypted)", required = true, example = "clear-text") @RequestParam String bucket,
		@ApiParam(value = "cos bucket key", required = true, example = "my-key") @RequestParam String key)
			throws IOException  {
		System.out.println("\n###   COS-STRING DOWNLOAD   ###");
		String data = new String(minioS3Client.downloadBytes(bucket, key));
		return data;
	}

@PostMapping("/cos/file-upload")
	@ApiOperation("Upload file to cos bucket with key")
	public String uploadFile(
			@ApiParam(value = "cos bucket name (clear-test or encrypted)", required = true, example = "clear-text") @RequestParam String bucket,
			@ApiParam(value = "cos bucket key", required = true, example = "my-file.pdf") @RequestParam String key,
			@ApiParam(value = "file to store in secret", required = true) @RequestPart(value = "file") MultipartFile file)
			throws IOException{
		System.out.println("\n###   COS-FILE-UPLOAD   ###");
		String result = minioS3Client.uploadBytes(bucket, key, file.getBytes());
		return result;
	}

	@PostMapping("/cos/file-download")
	@ApiOperation("Download file from cos bucket at key")
	public ResponseEntity<InputStreamResource> downloadFile(
		@ApiParam(value = "cos bucket name (clear-test or encrypted)", required = true, example = "clear-text") @RequestParam String bucket,
		@ApiParam(value = "cos bucket key", required = true, example = "my-file.pdf") @RequestParam String key)
			throws IOException  {
		System.out.println("\n###   COS-FILE-DOWNLOAD   ###");
		byte[] bytes = minioS3Client.downloadBytes(bucket, key);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/octet-stream");
        headers.add("Content-Disposition", "attachment; filename=" + key);

		InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(bytes));
		headers.setContentLength(bytes.length);
		return new ResponseEntity<InputStreamResource>(inputStreamResource, headers, HttpStatus.OK);
	}


	@PostMapping("/vault/transit/encrypt-string")
	@ApiOperation("Encrypt string with vault transit keyring")
	public String encryptStringTransit(
		@ApiParam(value = "data string to encrypt", required = true, example = "hello world") @RequestParam String data,
		@ApiParam(value = "transit keyring path", required = true, example = "vault-sample") @RequestParam String path)
	 		throws URISyntaxException {
		System.out.println("\n###   ENCRYPT-STRING-TRANSIT  ###");
		byte[] bytes = data.getBytes();
		String result = vaultTransitService.encrypt(path, bytes);
		return result;
	}

	@PostMapping("/vault/transit/decrypt-string")
	@ApiOperation("Decrypt string with vault transit keyring")
	public String decryptStringTransit(
		@ApiParam(value = "data string to encrypt", required = true,
			example = "vault:v1:9Q7KWk1/W9StN/92LE5fRY8tyRP2OVtHkFXtbQD6HalbgOVdik+n0CSlDjA=") @RequestParam String data,
		@ApiParam(value = "transit keyring path", required = true, example = "vault-sample") @RequestParam String path)
		 	throws URISyntaxException {
		System.out.println("\n###   DECRYPT-STRING-TRANSIT   ###");
		String result = new String(vaultTransitService.decrypt(path, data));
		return result;
	}


	@PostMapping("/bouncycastle/encrypt-string")
	@ApiOperation("Encrypt string using bouncy castle")
	public String bcEncryptString(
		@ApiParam(value = "data string to encrypt", required = true, example = "hello world") @RequestParam String data)
			throws URISyntaxException, IOException, NoSuchProviderException, SignatureException, NoSuchAlgorithmException, PGPException {
		System.out.println("\n###   BOUNCYCASTLE-ENCRYPT-STRING   ###");
		byte[] bytes = data.getBytes();
		String response = bouncyCastleService.encrypt(bytes);
		return response;
	}

	// TODO - fiw swagger copy/paster to input field on multiple lines
	@PostMapping("/bouncycastle/decrypt-string")
	@ApiOperation("Decrypt string using bouncy castle")
	public String bcDecryptString(
		@ApiParam(value = "data string to decrypt", required = true,
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
		System.out.println("\n###   BOUNCYCASTLE-DECRYPT-STRING   ###");
		String response = bouncyCastleService.decrypt(data);
		return response;
	}

	@PostMapping("/cos/bc/string-upload-encrypted")
	@ApiOperation("Upload string to cos bucket + key and with data encrypted with bouncy castle before it is stored")
	public String uploadBcStringEncrypted(
		@ApiParam(value = "cos bucket name (clear-test or encrypted)", required = true, example = "clear-text") @RequestParam String bucket,
		@ApiParam(value = "cos bucket key", required = true, example = "my-bc-encrypted") @RequestParam String key,
		@ApiParam(value = "data string to store", required = true, example = "hello world") @RequestParam String data)
			throws URISyntaxException, IOException, NoSuchProviderException, SignatureException, NoSuchAlgorithmException, PGPException {
		System.out.println("\n###   COS-UPLOAD-BC-STRING-ENCRYPTED   ###");
		String result = minioS3Client.uploadStringBcEncrypted(bucket, key, data);
		return result;
	}

	@PostMapping("/cos/bc/string-download-encrypted")
	@ApiOperation("Download string from cos bucket + key and with data decrypted with bouncy castle after it is retrieved")
	public String downloadBcStringDecrypted(
		@ApiParam(value = "cos bucket name (clear-test or encrypted)", required = true, example = "clear-text") @RequestParam String bucket,
		@ApiParam(value = "cos bucket key", required = true, example = "my-bc-encrypted") @RequestParam String key)
			throws IOException, NoSuchProviderException, SignatureException, PGPException  {
				System.out.println("\n###   COS-UPLOAD-BC-STRING-ENCRYPTED   ###");
		String data = minioS3Client.downloadStringBcEncrypted(bucket, key);
		return data;
	}

	@PostMapping("/cos/bc/file-upload-encrypted")
	@ApiOperation("Upload file to cos bucket + key and with data encrypted with bouncy castle before it is stored")
	public String uploadBcFileEncrypted(
			@ApiParam(value = "cos bucket name (clear-test or encrypted)", required = true, example = "clear-text") @RequestParam String bucket,
			@ApiParam(value = "cos bucket key", required = true, example = "my-bc-encrypted.pdf") @RequestParam String key,
			@ApiParam(value = "file to store in secret", required = true) @RequestPart(value = "file") MultipartFile file)
			throws URISyntaxException, IOException, NoSuchProviderException, SignatureException,
			NoSuchAlgorithmException, PGPException {
		System.out.println("\n###   COS-UPLOAD-BC-FILE-ENCRYPTED   ###");
		String result = minioS3Client.uploadFileBcEncrypted(bucket, key, file.getBytes());
		return result;
	}

	@PostMapping("/cos/bc/file-download-excrypted")
	@ApiOperation("Download file from cos bucket + key and with data decrypted with bouncy castle after it is retrieved")
	public ResponseEntity<InputStreamResource> downloadBcFileEncrypted(
			@ApiParam(value = "cos bucket name (clear-test or encrypted)", required = true, example = "clear-text") @RequestParam String bucket,
			@ApiParam(value = "cos bucket key", required = true, example = "my-bc-encrypted.pdf") @RequestParam String key)
			throws IOException, NoSuchProviderException, SignatureException, PGPException {
		System.out.println("\n###   COS-UPLOAD-BC-FILE-ENCRYPTED   ###");
		byte[] bytes = minioS3Client.downloadFileBcEncrypted(bucket, key);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/octet-stream");
		headers.add("Content-Disposition", "attachment; filename=" + key);

		InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(bytes));
		headers.setContentLength(bytes.length);
		return new ResponseEntity<InputStreamResource>(inputStreamResource, headers, HttpStatus.OK);
	}

	@PostMapping("/cos/transit/string-upload-encrypted")
	@ApiOperation("Upload string to cos bucket + key and with data encrypted using vault transit keyring")
	public String uploadTransitStringEncrypted(
		@ApiParam(value = "cos bucket name (clear-test or encrypted)", required = true, example = "clear-text") @RequestParam String bucket,
		@ApiParam(value = "cos bucket key", required = true, example = "my-transit-encrypted") @RequestParam String key,
		@ApiParam(value = "data string to store", required = true, example = "hello world") @RequestParam String data,
		@ApiParam(value = "transit keyring path", required = true, example = "vault-sample") @RequestParam String path)
			throws URISyntaxException, IOException, NoSuchProviderException, SignatureException, NoSuchAlgorithmException, PGPException {
		System.out.println("\n###   COS-UPLOAD-TRANSIT-ENCRYPTED   ###");
		String result = minioS3Client.uploadStringTransitEncrypted(bucket, key, data, path);
		return result;
	}

	@PostMapping("/cos/transit/string-download-encrypted")
	@ApiOperation("Download string from cos bucket + key and with data decrypted using vault transit keyring")
	public String downloadTransitStringDecrypted(
		@ApiParam(value = "cos bucket name (clear-test or encrypted)", required = true, example = "clear-text") @RequestParam String bucket,
		@ApiParam(value = "cos bucket key", required = true, example = "my-transit-encrypted") @RequestParam String key,
		@ApiParam(value = "transit keyring path", required = true, example = "vault-sample") @RequestParam String path)
			throws IOException, NoSuchProviderException, SignatureException, PGPException  {
				System.out.println("\n###   COS-UPLOAD-TRANSIT-ENCRYPTED   ###");
		String data = minioS3Client.downloadStringTransitEncrypted(bucket, key, path);
		return data;
	}

	@PostMapping("/cos/transit/file-upload-encrypted")
	@ApiOperation("Upload file to cos bucket + key and with data encrypted using vault transit keyring")
	public String uploadTransitFileEncrypted(
			@ApiParam(value = "cos bucket name (clear-test or encrypted)", required = true, example = "clear-text") @RequestParam String bucket,
			@ApiParam(value = "cos bucket key", required = true, example = "my-transit-encrypted.pdf") @RequestParam String key,
			@ApiParam(value = "file to store in secret", required = true) @RequestPart(value = "file") MultipartFile file,
			@ApiParam(value = "transit keyring path", required = true, example = "vault-sample") @RequestParam String path)
			throws URISyntaxException, IOException, NoSuchProviderException, SignatureException,
			NoSuchAlgorithmException, PGPException {
		System.out.println("\n###   COS-UPLOAD-TRANSIT-FILE-ENCRYPTED   ###");
		String result = minioS3Client.uploadFileTransitEncrypted(bucket, key, file.getBytes(), path);
		return result;
	}

	@PostMapping("/cos/transit/file-download-excrypted")
	@ApiOperation("Download file from cos bucket + key and with data encrypted using vault transit keyring")
	public ResponseEntity<InputStreamResource> downloadBcFileEncrypted(
			@ApiParam(value = "cos bucket name (clear-test or encrypted)", required = true, example = "clear-text") @RequestParam String bucket,
			@ApiParam(value = "cos bucket key", required = true, example = "my-transit-encrypted.pdf") @RequestParam String key,
			@ApiParam(value = "transit keyring path", required = true, example = "vault-sample") @RequestParam String path)
			throws IOException  {
		System.out.println("\n###   COS-UPLOAD-TRANSIT-FILE-ENCRYPTED   ###");
		byte[] bytes = minioS3Client.downloadFileTransitEncrypted(bucket, key, path);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/octet-stream");
		headers.add("Content-Disposition", "attachment; filename=" + key);

		InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(bytes));
		headers.setContentLength(bytes.length);
		return new ResponseEntity<InputStreamResource>(inputStreamResource, headers, HttpStatus.OK);
	}

	@PostMapping("/cos/transit/local/string-upload-encrypted")
	@ApiOperation("Upload string to cos bucket + key and with data encrypted locally using vault transit datakey")
	public String uploadTransitLocalStringEncrypted(
		@ApiParam(value = "cos bucket name (clear-test or encrypted)", required = true, example = "clear-text") @RequestParam String bucket,
		@ApiParam(value = "cos bucket key", required = true, example = "my-transit-encrypted") @RequestParam String key,
		@ApiParam(value = "data string to store", required = true, example = "hello world") @RequestParam String data,
		@ApiParam(value = "transit keyring path", required = true, example = "vault-sample") @RequestParam String path)
			throws URISyntaxException, IOException, NoSuchProviderException, SignatureException, NoSuchAlgorithmException, PGPException {
		System.out.println("\n###   COS-UPLOAD-TRANSIT-LOCAL-ENCRYPTED   ###");
		String result = minioS3Client.uploadStringTransitLocalEncrypted(bucket, key, data, path);
		return result;
	}

	@PostMapping("/cos/transit/local/string-download-encrypted")
	@ApiOperation("Download string from cos bucket + key and with data decrypted locally using vault transit datakey")
	public String downloadTransitLocalDecrypted(
		@ApiParam(value = "cos bucket name (clear-test or encrypted)", required = true, example = "clear-text") @RequestParam String bucket,
		@ApiParam(value = "cos bucket key", required = true, example = "my-transit-encrypted") @RequestParam String key,
		@ApiParam(value = "transit keyring path", required = true, example = "vault-sample") @RequestParam String path)
			throws IOException, NoSuchProviderException, SignatureException, PGPException  {
				System.out.println("\n###   COS-UPLOAD-TRANSIT-LOCAL-ENCRYPTED   ###");
		String data = minioS3Client.downloadStringTransitLocalEncrypted(bucket, key, path);
		return data;
	}



}
