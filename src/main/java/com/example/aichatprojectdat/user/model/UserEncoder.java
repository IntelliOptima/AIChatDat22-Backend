package com.example.aichatprojectdat.user.model;

import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractDecoder;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public class UserEncoder extends AbstractDecoder<User> {
    @Override
    public Flux<User> decode(Publisher<DataBuffer> inputStream, ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
        return null;
    }

    @Override
    public User decode(DataBuffer buffer, ResolvableType targetType, MimeType mimeType, Map<String, Object> hints) throws DecodingException {
        return super.decode(buffer, targetType, mimeType, hints);
    }

    @Override
    public List<MimeType> getDecodableMimeTypes(ResolvableType targetType) {
        return super.getDecodableMimeTypes(targetType);
    }
}
