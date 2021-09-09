package com.garage.springboot;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.net.URISyntaxException;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
public class HelloControllerTest {

	@Autowired
	private MockMvc mvc;

	private final static String TRUST_STORE_FILE = "vault-truststore.jks";
    private final static String TRUST_STORE_PASSWORD = "passw0rd";


    @BeforeAll
    public static void setUpAll() throws URISyntaxException {
        URI trustStoreURI = HelloControllerTest.class.getClassLoader().getResource(TRUST_STORE_FILE).toURI();
        System.setProperty("javax.net.ssl.trustStore", new File(trustStoreURI).getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", TRUST_STORE_PASSWORD);
    }

	@Test
	public void getHello() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/hello").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("Greetings from Spring Boot!")));
	}
}
