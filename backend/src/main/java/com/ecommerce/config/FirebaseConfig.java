package com.ecommerce.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {
    
    @Value("${firebase.service-account-key}")
    private String serviceAccountKey;
    
    @PostConstruct
    public void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                GoogleCredentials credentials;
                
                if (serviceAccountKey != null && !serviceAccountKey.isEmpty()) {
                    InputStream serviceAccount = new ByteArrayInputStream(serviceAccountKey.getBytes());
                    credentials = GoogleCredentials.fromStream(serviceAccount);
                    
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(credentials)
                            .build();
                    
                    FirebaseApp.initializeApp(options);
                    log.info("Firebase application initialized with service account");
                } else {
                    log.warn("Firebase service account key not provided. Firebase features will be disabled for demo.");
                    log.warn("To enable Firebase, set FIREBASE_SERVICE_ACCOUNT_KEY environment variable");
                }
            }
        } catch (Exception e) {
            log.warn("Firebase initialization failed: {}. Firebase features will be disabled.", e.getMessage());
            log.warn("To enable Firebase, provide valid Firebase credentials");
        }
    }
    
    @Bean
    public FirebaseAuth firebaseAuth() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                return FirebaseAuth.getInstance();
            } else {
                log.warn("Firebase not initialized, returning null for FirebaseAuth bean");
                return null;
            }
        } catch (Exception e) {
            log.warn("Failed to get FirebaseAuth instance: {}", e.getMessage());
            return null;
        }
    }
}
