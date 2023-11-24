package com.example.aichatprojectdat.config.auth.oauth2.authorized_clients.model;

import java.util.Map;
import java.util.Set;

import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;


public final class ClientRegistration {

    private String registrationId;
    private String clientId;
    private String clientSecret;
    private ClientAuthenticationMethod clientAuthenticationMethod;
    private AuthorizationGrantType authorizationGrantType;
    private String redirectUri;
    private Set<String> scopes;
    private ProviderDetails providerDetails;
    private String clientName;

    public class ProviderDetails {
        private String authorizationUri;
        private String tokenUri;
        private UserInfoEndpoint userInfoEndpoint;
        private String jwkSetUri;
        private String issuerUri;
        private Map<String, Object> configurationMetadata;

        public class UserInfoEndpoint {
            private String uri;
            private AuthenticationMethod authenticationMethod;
            private String userNameAttributeName;
        }
    }
    
}
