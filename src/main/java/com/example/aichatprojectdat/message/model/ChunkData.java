package com.example.aichatprojectdat.message.model;

public record ChunkData(
        String identifier,
        Message chunk,
        Long startIndex,
        Long totalChunks,
        Boolean isLastChunk

) {
    public static ChunkData of(String identifier, Message chunk, Long startIndex, Long totalChunks, Boolean isLastChunk) {
        return new ChunkData(identifier, chunk, startIndex, totalChunks, isLastChunk);
    }
}
