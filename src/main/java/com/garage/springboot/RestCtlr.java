package com.garage.springboot;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.garage.pojo.UserPwd;
import com.garage.vault.VaultSecretsService;
import com.garage.vault.VaultTransitService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestCtlr {

	@Autowired
	VaultSecretsService vaultSecretsService;
	@Autowired
	VaultTransitService vaultTransitService;

	@GetMapping("/hello")
	public String index() {
		System.out.println("###   HELLO WORLD   ###");
		return "Greetings from Spring Boot!";
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

}
