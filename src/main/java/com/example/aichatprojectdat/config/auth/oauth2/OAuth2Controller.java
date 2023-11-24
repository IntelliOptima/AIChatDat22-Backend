package com.example.aichatprojectdat.config.auth.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.R2dbcReactiveOAuth2AuthorizedClientService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
public class OAuth2Controller {

    @Autowired
    R2dbcReactiveOAuth2AuthorizedClientService authorizedClientService;
    @GetMapping("/")
    public Mono<String> index(Authentication authentication) {
        return this.authorizedClientService.loadAuthorizedClient("google", authentication.getName())
                .map(OAuth2AuthorizedClient::getAccessToken)

				.thenReturn("index");
    }

}
