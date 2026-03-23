package com.example.PlugLess.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.PlugLess.dto.friend.FriendPageResponse;
import com.example.PlugLess.dto.friend.FriendRequestResponse;
import com.example.PlugLess.dto.friend.FriendRequestPageResponse;
import com.example.PlugLess.dto.user.PublicProfileResponse;
import com.example.PlugLess.entity.User;
import com.example.PlugLess.repository.UserRepository;

@Service
public class FriendService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final UserRepository userRepository;
    private final UserService userService;

    public FriendService(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    private User getByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private User getById(String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public void sendRequest(String senderEmail, String targetId) {
        User sender = getByEmail(senderEmail);
        User target = getById(targetId);
        List<String> senderFriendIds = mutableList(sender.getFriendIds());
        List<String> senderRequestIds = mutableList(sender.getFriendRequestIds());
        List<String> targetRequestIds = mutableList(target.getFriendRequestIds());

        // can't send to yourself
        if (sender.getId().equals(targetId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot send request to yourself");

        // already friends
        if (senderFriendIds.contains(targetId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already friends");

        // I already sent them a request — don't duplicate
        if (targetRequestIds.contains(sender.getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Friend request already sent");

        // target already sent a request to sender; user must accept/reject that incoming request
        if (senderRequestIds.contains(targetId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already sent you a request. Accept or reject it.");

        // normal case — add sender's ID to target's incoming requests
        targetRequestIds.add(sender.getId());
        target.setFriendRequestIds(targetRequestIds);
        userRepository.save(target);
    }

    public void acceptRequest(String myEmail, String requesterId) {
        User me = getByEmail(myEmail);
        User requester = getById(requesterId);
        List<String> myRequestIds = mutableList(me.getFriendRequestIds());
        List<String> myFriendIds = mutableList(me.getFriendIds());
        List<String> requesterFriendIds = mutableList(requester.getFriendIds());

        if (myFriendIds.contains(requesterId)) {
            if (myRequestIds.remove(requesterId)) {
                me.setFriendRequestIds(myRequestIds);
                userRepository.save(me);
            }
            return;
        }

        if (!myRequestIds.contains(requesterId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No friend request from this user");

        myFriendIds.add(requesterId);
        requesterFriendIds.add(me.getId());
        myRequestIds.remove(requesterId);

        me.setFriendIds(myFriendIds);
        me.setFriendRequestIds(myRequestIds);
        requester.setFriendIds(requesterFriendIds);

        userRepository.save(me);
        userRepository.save(requester);
    }

    public void rejectRequest(String myEmail, String requesterId) {
        User me = getByEmail(myEmail);
        List<String> myRequestIds = mutableList(me.getFriendRequestIds());

        if (!myRequestIds.contains(requesterId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No friend request from this user");

        myRequestIds.remove(requesterId);
        me.setFriendRequestIds(myRequestIds);
        userRepository.save(me);
    }

    public void removeFriend(String myEmail, String friendId) {
        User me = getByEmail(myEmail);
        User friend = getById(friendId);
        List<String> myFriendIds = mutableList(me.getFriendIds());
        List<String> friendFriendIds = mutableList(friend.getFriendIds());

        if (!myFriendIds.contains(friendId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not in your friends list");

        myFriendIds.remove(friendId);
        friendFriendIds.remove(me.getId());

        me.setFriendIds(myFriendIds);
        friend.setFriendIds(friendFriendIds);

        userRepository.save(me);        // save both — both documents changed
        userRepository.save(friend);
    }

    public FriendPageResponse getMyFriends(String myEmail, int page, int size) {
        User me = getByEmail(myEmail);
        List<String> friendIds = mutableList(me.getFriendIds());
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);

        if (friendIds.isEmpty()) {
            return FriendPageResponse.empty(normalizedPage, normalizedSize);
        }

        List<String> pagedIds = pageIds(friendIds, normalizedPage, normalizedSize);
        if (pagedIds.isEmpty()) {
            return FriendPageResponse.empty(normalizedPage, normalizedSize, friendIds.size());
        }

        Map<String, User> byId = userRepository.findAllByIdIn(pagedIds)
            .stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));

        List<PublicProfileResponse> friends = pagedIds.stream()
            .map(byId::get)
            .filter(java.util.Objects::nonNull)
            .map(userService::toPublicProfile)
            .collect(Collectors.toList());

        return FriendPageResponse.of(friends, normalizedPage, normalizedSize, friendIds.size());
    }

    public FriendRequestPageResponse getIncomingRequests(String myEmail, int page, int size) {
        User me = getByEmail(myEmail);
        List<String> requesterIds = mutableList(me.getFriendRequestIds());
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);

        if (requesterIds.isEmpty()) {
            return FriendRequestPageResponse.empty(normalizedPage, normalizedSize);
        }

        List<String> pagedIds = pageIds(requesterIds, normalizedPage, normalizedSize);
        if (pagedIds.isEmpty()) {
            return FriendRequestPageResponse.empty(normalizedPage, normalizedSize, requesterIds.size());
        }

        Map<String, User> byId = userRepository.findAllByIdIn(pagedIds)
            .stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));

        List<FriendRequestResponse> requests = pagedIds.stream()
            .map(byId::get)
            .filter(java.util.Objects::nonNull)
            .map(this::toRequestResponse)
            .collect(Collectors.toList());

        return FriendRequestPageResponse.of(requests, normalizedPage, normalizedSize, requesterIds.size());
    }

    private FriendRequestResponse toRequestResponse(User u) {
        return new FriendRequestResponse(
            u.getId(),
            u.getUserName(),
            u.getDisplayName(),
            u.getProfileImageUrl(),
            u.isOnline(),
            u.getLastSeen()
        );
    }

    private List<String> mutableList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(values);
    }

    private int normalizePage(int page) {
        return Math.max(0, page);
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private List<String> pageIds(List<String> ids, int page, int size) {
        int fromIndex = page * size;
        if (fromIndex >= ids.size()) {
            return Collections.emptyList();
        }

        int toIndex = Math.min(fromIndex + size, ids.size());
        return ids.subList(fromIndex, toIndex);
    }
}
