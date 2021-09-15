package com.garage.spring;

import com.garage.cos.MinioS3Client;
import com.garage.vault.VaultSecretsService;
import com.garage.vault.VaultTransitService;
import com.garage.crypt.BouncyCastleService;

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
	public MinioS3Client minioS3Client() {
		return new MinioS3Client();
	}

	@Bean
	public BouncyCastleService bouncyCastleService(){
		return new BouncyCastleService();
	}
}
