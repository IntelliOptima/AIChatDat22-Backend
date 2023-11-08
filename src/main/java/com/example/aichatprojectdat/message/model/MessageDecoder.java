package com.example.aichatprojectdat.message.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractDecoder;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MessageDecoder extends AbstractDecoder<Message> {

    private final ObjectMapper objectMapper;

    public MessageDecoder(ObjectMapper objectMapper) {
        super(MimeType.valueOf("application/octet-stream"));
        this.objectMapper = objectMapper;
    }

    @Override
    @NonNull
    public Flux<Message> decode(@NonNull Publisher<DataBuffer> inputStream, @NonNull ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
        return Flux.from(inputStream)
                .map(dataBuffer -> {
                    try {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        String json = new String(bytes, StandardCharsets.UTF_8);
                        return objectMapper.readValue(json, Message.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to decode", e);
                    } finally {
                        DataBufferUtils.release(dataBuffer);
                    }
                });
    }

    @Override
    public Message decode(DataBuffer buffer, @NonNull ResolvableType targetType, MimeType mimeType, Map<String, Object> hints) throws DecodingException {
        try {
            byte[] bytes = new byte[buffer.readableByteCount()];
            buffer.read(bytes);
            String json = new String(bytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, Message.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to decode", e);
        } finally {
            DataBufferUtils.release(buffer);
        }
    }

    @Override
    @NonNull
    public List<MimeType> getDecodableMimeTypes() {
        return Collections.singletonList(MimeType.valueOf("application/octet-stream"));
    }

}