package com.example.aichatprojectdat.utilities;

import com.example.aichatprojectdat.message.model.ChunkData;
import com.example.aichatprojectdat.message.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ReactiveWebsocketMethods {
    private static final Pattern stableDiffusionModelPattern = Pattern.compile("@\\w+=([^ ]+)");

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

    public boolean isGeminiMessage(List<ChunkData> chunkDataList) {
        log.info(chunkDataList.toString());
        Message chunk = chunkDataList.get(chunkDataList.size() - 1).chunk();
        return chunk != null && chunk.getTextMessage().toLowerCase().startsWith("@gemini");
    }

    public String extractModelId(String input) {
        Matcher matcher = stableDiffusionModelPattern.matcher(input);
        if (matcher.find()) {
            // The first group (index 1) captures the model ID following the '='
            return matcher.group(1).trim();
        } else {
            return null;
        }
    }
}
