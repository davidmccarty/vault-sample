package com.garage.spring;

import com.garage.cos.S3Client;
import com.garage.vault.VaultSecretsService;
import com.garage.vault.VaultTransitService;
import com.garage.crypt.BCCrypt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

	@Bean
	public VaultSecretsService vaultSecretsService() {
		return new VaultSecretsService();
	}

	@Bean
	public VaultTransitService vaultTransitService() {
		return new VaultTransitService();
	}

	@Bean
	public S3Client minioS3Client() {
		return new S3Client();
	}

	@Bean
	public BCCrypt bouncyCastleService(){
		return new BCCrypt();
	}
}
