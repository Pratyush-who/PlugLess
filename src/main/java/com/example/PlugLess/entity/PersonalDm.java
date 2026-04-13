package com.example.PlugLess.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document("personal_dms")
@Getter
@Setter
@NoArgsConstructor
public class PersonalDm {

    @Id
    private String id; // format: user1Id::user2Id

    private String user1Id;
    private String user2Id;

    private Instant lastUpdated;

    private List<DirectMessage> messages = new ArrayList<>();
}

