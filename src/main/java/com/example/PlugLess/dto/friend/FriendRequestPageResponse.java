package com.example.PlugLess.dto.friend;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FriendRequestPageResponse {
    private final List<FriendRequestResponse> requests;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;

    public static FriendRequestPageResponse of(List<FriendRequestResponse> requests, int page, int size, long totalElements) {
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        boolean hasNext = page + 1 < totalPages;
        return new FriendRequestPageResponse(requests, page, size, totalElements, totalPages, hasNext);
    }

    public static FriendRequestPageResponse empty(int page, int size) {
        return of(List.of(), page, size, 0);
    }

    public static FriendRequestPageResponse empty(int page, int size, long totalElements) {
        return of(List.of(), page, size, totalElements);
    }
}

