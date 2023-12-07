package com.example.aichatprojectdat.config;

import com.example.aichatprojectdat.message.model.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChunkData {
    private String chunkIdentifier;
    private Message chunk;
    private Long startIndex;
    private boolean isLastChunk;
}
