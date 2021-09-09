package com.garage.springboot;

import com.garage.vault.VaultSecretsService;
import com.garage.vault.VaultTransitService;

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
}
