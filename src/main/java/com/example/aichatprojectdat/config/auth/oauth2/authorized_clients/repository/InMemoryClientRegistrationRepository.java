package com.example.aichatprojectdat.config.auth.oauth2.authorized_clients.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryClientRegistrationRepository implements ClientRegistrationRepository {

    private final Map<String, ClientRegistration> clientRegistrations = new ConcurrentHashMap<>();

    // This method allows adding client registrations to the repository
    public void addClientRegistration(ClientRegistration clientRegistration) {
        clientRegistrations.put(clientRegistration.getRegistrationId(), clientRegistration);
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
       return clientRegistrations.get(registrationId);
    }
    
}
