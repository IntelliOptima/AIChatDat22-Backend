package com.example.aichatprojectdat.utilities;

import com.example.aichatprojectdat.message.model.ChunkData;
import com.example.aichatprojectdat.message.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ReactiveWebsocketMethods {

    private final ObjectMapper jsonMapper;

    public ReactiveWebsocketMethods(@Qualifier("jsonObjectMapperUtil") ObjectMapper objectMapper) {
        this.jsonMapper = objectMapper;
    }



    public boolean isCompleteMessage(String identifier, Map<String, List<ChunkData>> chunkStream ) {
        List<ChunkData> chunks = chunkStream.get(identifier);
        if (chunks == null || chunks.isEmpty()) {
            return false;
        }

        long totalChunks = chunks.get(0).totalChunks();
        return chunks.size() == totalChunks;
    }


    public boolean isGptMessage(List<ChunkData> chunkDataList) {
        log.info(chunkDataList.toString());
        Message chunk = chunkDataList.get(chunkDataList.size() - 1).chunk();
        return chunk != null && chunk.getTextMessage().toLowerCase().startsWith("@gpt");
    }

    public boolean isDalleMessage(List<ChunkData> chunkDataList) {
        log.info(chunkDataList.toString());
        Message chunk = chunkDataList.get(chunkDataList.size() - 1).chunk();
        return chunk != null && chunk.getTextMessage().toLowerCase().startsWith("@dalle");
    }

    public boolean isLastChunkReceived(List<ChunkData> chunkDataList) {
        if (chunkDataList.size() == chunkDataList.get(0).totalChunks()) {
            return chunkDataList.stream().anyMatch(ChunkData::isLastChunk);
        } else {
            return false;
        }
    }

    public ChunkData convertToChunkData(String messageContent) {
        try {
            log.info("processMessage received: {} ", messageContent);
            return jsonMapper.readValue(messageContent, ChunkData.class);
        } catch (JsonProcessingException e) {
            return ChunkData.empty();
        }
    }


    public String extractChatroomId(String uriPath) {
        // Extract the chatroomId from the URI path
        return uriPath.substring(uriPath.lastIndexOf('/') + 1);
    }


}
