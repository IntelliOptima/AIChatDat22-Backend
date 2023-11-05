package com.example.aichatprojectdat.rsocket;

import io.rsocket.core.RSocketServer;
import io.rsocket.core.Resume;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("resumption")
@Component
public class RSocketServerResumptionConfig implements RSocketServerCustomizer {
    @Override
    public void customize(RSocketServer rSocketServer) {
        rSocketServer.resume(new Resume());
    }
}
