package com.example.PlugLess.dto.friend;

import java.util.List;

import com.example.PlugLess.dto.user.PublicProfileResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FriendPageResponse {
    private final List<PublicProfileResponse> friends;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;

    public static FriendPageResponse of(List<PublicProfileResponse> friends, int page, int size, long totalElements) {
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        boolean hasNext = page + 1 < totalPages;
        return new FriendPageResponse(friends, page, size, totalElements, totalPages, hasNext);
    }

    public static FriendPageResponse empty(int page, int size) {
        return of(List.of(), page, size, 0);
    }

    public static FriendPageResponse empty(int page, int size, long totalElements) {
        return of(List.of(), page, size, totalElements);
    }
}

