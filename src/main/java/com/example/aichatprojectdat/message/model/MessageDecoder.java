package com.example.aichatprojectdat.message.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractDecoder;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MessageDecoder extends AbstractDecoder<List<Message>> {

    private final ObjectMapper objectMapper;

    public MessageDecoder(ObjectMapper objectMapper) {
        super(MimeType.valueOf("application/json"));
        this.objectMapper = objectMapper;
    }

    @Override
    @NonNull
    public Flux<List<Message>> decode(@NotNull Publisher<DataBuffer> inputStream,
                                      @NotNull ResolvableType elementType,
                                      MimeType mimeType,
                                      Map<String, Object> hints) {

        return DataBufferUtils.join(inputStream) // Join all DataBuffers into one
                .flux() // Convert Mono<DataBuffer> to Flux<DataBuffer>
                .flatMap(dataBuffer -> {
                    try {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer); // Release the dataBuffer

                        String json = new String(bytes, StandardCharsets.UTF_8);
                        List<Message> messages = objectMapper.readValue(json, new TypeReference<List<Message>>() {});
                        return Flux.just(messages); // Return a Flux containing the list of messages
                    } catch (IOException e) {
                        return Flux.error(new DecodingException("Failed to decode", e));
                    }
                });
    }

    @Override
    public List<Message> decode(DataBuffer buffer,
                                @NonNull ResolvableType targetType,
                                MimeType mimeType,
                                Map<String, Object> hints) throws DecodingException {
        try {
            byte[] bytes = new byte[buffer.readableByteCount()];
            buffer.read(bytes);
            String json = new String(bytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, new TypeReference<List<Message>>() {});
        } catch (IOException e) {
            throw new DecodingException("Failed to decode", e);
        } finally {
            DataBufferUtils.release(buffer);
        }
    }

    @Override
    @NonNull
    public List<MimeType> getDecodableMimeTypes() {
        return Collections.singletonList(MimeType.valueOf("application/json"));
    }
}
