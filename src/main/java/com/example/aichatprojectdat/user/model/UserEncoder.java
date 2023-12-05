package com.example.aichatprojectdat.user.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractEncoder;
import org.springframework.core.codec.EncodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UserEncoder extends AbstractEncoder<User> {
    private final ObjectMapper objectMapper;

    public UserEncoder(ObjectMapper objectMapper) {
        super(MimeType.valueOf("application/json"));
        this.objectMapper = objectMapper;
    }


    @Override
    @NonNull
    public Flux<DataBuffer> encode(@NonNull Publisher<? extends User> inputStream, @NonNull DataBufferFactory bufferFactory, @NonNull ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
        return Flux.from(inputStream)
                .flatMap(user -> {
                    try {
                        byte[] bytes = objectMapper.writeValueAsBytes(user);
                        DataBuffer buffer = bufferFactory.wrap(bytes);
                        return Mono.just(buffer);
                    } catch (Exception e) {
                        return Flux.error(new EncodingException("Could not write JSON: " + e.getMessage(), e));
                    }
                });
    }

    @Override
    @NonNull
    public DataBuffer encodeValue(@NonNull User value, @NonNull DataBufferFactory bufferFactory, @NonNull ResolvableType valueType, MimeType mimeType, Map<String, Object> hints) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(value);
            return bufferFactory.wrap(bytes);
        } catch (Exception e) {
            throw new EncodingException("Could not encode Message to JSON", e);
        }
    }

    @Override
    @NonNull
    public List<MimeType> getEncodableMimeTypes() {
        return Collections.singletonList(MimeType.valueOf("application/json"));
    }
}
