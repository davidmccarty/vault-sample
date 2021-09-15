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
import com.garage.data.UserPwd;
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


@RestController
@Api(description = "Tests for vault and cos")
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

	@PostMapping("/cos/upload")
	@ApiOperation("Upload string to cos bucket with key")
	public String upload(String bucket, String key, String data) throws URISyntaxException, IOException {
		System.out.println("###   COS-UPLOAD   ###");
		minioS3Client.upload(bucket, key, data);
		return "Upload successful";
	}

	@PostMapping("/cos/download")
	@ApiOperation("Download string from cos bucket at key")
	public String download(String bucket, String key) throws IOException  {
		String data = minioS3Client.download(bucket, key);
		return data;
	}

	@GetMapping("/vault/put-secret")
	public String putSecret() throws URISyntaxException {
		System.out.println("###   PUT-SECRET   ###");
		Calendar calendar = Calendar.getInstance();
		String user = "user-" + Integer.toString(calendar.get(Calendar.MINUTE));
		String pwd = "password-" + Integer.toString(calendar.get(Calendar.SECOND));
		UserPwd secret = new UserPwd(user, pwd);
		String path = "secret/spring-vault";
		vaultSecretsService.putSecret(path, (Object)secret);
		return "Put secret successful";
	}

	@GetMapping("/vault/get-secret")
	public String getSecret() throws URISyntaxException {
		System.out.println("###   GET-SECRET   ###");
		String path = "secret/spring-vault";
		UserPwd userPwd = vaultSecretsService.getSecret(path, UserPwd.class);
		return "Got secret user=" + userPwd.getUsername() + " password=" + userPwd.getPassword();
	}

	@GetMapping("/vault/encrypt")
	public String encrypt() throws URISyntaxException {
		System.out.println("###   ENCRYPT   ###");
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
		byte[] bytes = formatter.format(calendar.getTime()).getBytes();
		String path = "dev-ring";
		String result = vaultTransitService.encrypt(path, bytes);
		return "Encryption result = " + result;
	}

	@GetMapping("/vault/decrypt")
	public String decrypt(@RequestParam String data) throws URISyntaxException {
		System.out.println("###   DECRYPT   ###");
		String path = "dev-ring";
		String result = new String(vaultTransitService.decrypt(path, data));
		return "Decryption result = " + result;
	}

	@GetMapping("/vault/encryptLocal")
	public String encryptLocal() throws URISyntaxException, NoSuchAlgorithmException {
		System.out.println("###   ENCRYPT LOCAL   ###");
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
		byte[] bytes = formatter.format(calendar.getTime()).getBytes();
		String path = "dev-ring";
		String result = vaultTransitService.encryptLocal(path, bytes);  // TODO
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
