package com.example.aichatprojectdat;


import java.time.Instant;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import lombok.*;

import org.springframework.data.annotation.Id;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {


        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String message;

        private Long sent = Instant.now().getEpochSecond();


}