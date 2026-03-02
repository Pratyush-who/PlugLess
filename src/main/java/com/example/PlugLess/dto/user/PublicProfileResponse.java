package com.example.PlugLess.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublicProfileResponse {
    private String id;
    private String userName;
    private String displayName;
    private String bio;
    private String status;
    private String profileImageUrl;
    private int friendCount;
}

