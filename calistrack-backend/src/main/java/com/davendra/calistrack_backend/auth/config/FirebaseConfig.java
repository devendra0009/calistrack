package com.davendra.calistrack_backend.auth.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

	@PostConstruct
	public void initialize() {
		try {
			String firebaseConfigBase64 = System.getenv("FIREBASE_CONFIG_BASE64");
			if (firebaseConfigBase64 == null || firebaseConfigBase64.isBlank()) {
				throw new RuntimeException("FIREBASE_CONFIG_BASE64 env variable is missing");
			}

			InputStream serviceAccount = new ByteArrayInputStream(
					Base64.getDecoder().decode(firebaseConfigBase64)
			);

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.build();

			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseApp.initializeApp(options);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize Firebase", e);
		}
	}
}
