package com.example.PlugLess.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;
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
import com.example.PlugLess.dto.friend.SentFriendRequestPageResponse;
import com.example.PlugLess.dto.friend.SentFriendRequestResponse;
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

    public void cancelOutgoingRequest(String myEmail, String targetId) {
        User me = getByEmail(myEmail);
        User target = getById(targetId);
        List<String> targetRequestIds = mutableList(target.getFriendRequestIds());

        if (me.getId().equals(targetId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel request to yourself");
        }

        if (!targetRequestIds.contains(me.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No outgoing friend request for this user");
        }

        targetRequestIds.remove(me.getId());
        target.setFriendRequestIds(targetRequestIds);
        userRepository.save(target);
    }

    public void acceptRequest(String myEmail, String requesterId) {
        User me = getByEmail(myEmail);
        User requester = getById(requesterId);
        List<String> myRequestIds = mutableList(me.getFriendRequestIds());
        List<String> myFriendIds = mutableList(me.getFriendIds());
        List<String> requesterFriendIds = mutableList(requester.getFriendIds());
        List<String> requesterRequestIds = mutableList(requester.getFriendRequestIds());

        if (myFriendIds.contains(requesterId)) {
            if (myRequestIds.remove(requesterId)) {
                me.setFriendRequestIds(myRequestIds);
                userRepository.save(me);
            }
            return;
        }

        if (!myRequestIds.contains(requesterId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No friend request from this user");

        addUnique(myFriendIds, requesterId);
        addUnique(requesterFriendIds, me.getId());
        myRequestIds.remove(requesterId);
        requesterRequestIds.remove(me.getId());

        me.setFriendIds(myFriendIds);
        me.setFriendRequestIds(myRequestIds);
        requester.setFriendIds(requesterFriendIds);
        requester.setFriendRequestIds(requesterRequestIds);

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
        List<String> myRequestIds = mutableList(me.getFriendRequestIds());
        List<String> friendRequestIds = mutableList(friend.getFriendRequestIds());

        if (!myFriendIds.contains(friendId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not in your friends list");

        myFriendIds.remove(friendId);
        friendFriendIds.remove(me.getId());
        // Cleanup stale pending requests between the pair if any remain due to older inconsistent states.
        myRequestIds.remove(friendId);
        friendRequestIds.remove(me.getId());

        me.setFriendIds(myFriendIds);
        me.setFriendRequestIds(myRequestIds);
        friend.setFriendIds(friendFriendIds);
        friend.setFriendRequestIds(friendRequestIds);

        userRepository.save(me);        // save both — both documents changed
        userRepository.save(friend);
    }

    public FriendPageResponse getMyFriends(String myEmail, int page, int size) {
        User me = getByEmail(myEmail);
        List<String> friendReferences = mutableList(me.getFriendIds());
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);

        if (friendReferences.isEmpty()) {
            return FriendPageResponse.empty(normalizedPage, normalizedSize);
        }

        // Backward-compatible handling for clients that accidentally send one-based page numbers.
        int effectivePage = normalizedPage;
        List<String> pagedReferences = pageIds(friendReferences, effectivePage, normalizedSize);
        if (pagedReferences.isEmpty() && normalizedPage > 0) {
            int oneBasedAdjustedPage = normalizedPage - 1;
            List<String> fallbackReferences = pageIds(friendReferences, oneBasedAdjustedPage, normalizedSize);
            if (!fallbackReferences.isEmpty()) {
                effectivePage = oneBasedAdjustedPage;
                pagedReferences = fallbackReferences;
            }
        }

        if (pagedReferences.isEmpty()) {
            return FriendPageResponse.empty(effectivePage, normalizedSize, friendReferences.size());
        }

        Map<String, User> byReference = resolveUsersByReference(pagedReferences);

        List<PublicProfileResponse> friends = pagedReferences.stream()
            .map(byReference::get)
            .filter(java.util.Objects::nonNull)
            .map(userService::toPublicProfile)
            .collect(Collectors.toList());

        return FriendPageResponse.of(friends, effectivePage, normalizedSize, friendReferences.size());
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

    public SentFriendRequestPageResponse getSentRequests(String myEmail, int page, int size) {
        User me = getByEmail(myEmail);
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);

        List<User> targets = userRepository.findAllByFriendRequestIdsContaining(me.getId())
            .stream()
            .filter(target -> !me.getId().equals(target.getId()))
            .sorted(Comparator.comparing(User::getUserName, Comparator.nullsLast(String::compareToIgnoreCase)))
            .toList();

        if (targets.isEmpty()) {
            return SentFriendRequestPageResponse.empty(normalizedPage, normalizedSize);
        }

        int fromIndex = normalizedPage * normalizedSize;
        if (fromIndex >= targets.size()) {
            return SentFriendRequestPageResponse.empty(normalizedPage, normalizedSize, targets.size());
        }

        int toIndex = Math.min(fromIndex + normalizedSize, targets.size());
        List<SentFriendRequestResponse> requests = targets.subList(fromIndex, toIndex)
            .stream()
            .map(this::toSentRequestResponse)
            .collect(Collectors.toList());

        return SentFriendRequestPageResponse.of(requests, normalizedPage, normalizedSize, targets.size());
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

    private SentFriendRequestResponse toSentRequestResponse(User u) {
        return new SentFriendRequestResponse(
            u.getId(),
            u.getUserName(),
            u.getDisplayName(),
            u.getProfileImageUrl(),
            u.isOnline(),
            u.getLastSeen()
        );
    }

    private void addUnique(List<String> values, String id) {
        if (!values.contains(id)) {
            values.add(id);
        }
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

    private Map<String, User> resolveUsersByReference(List<String> references) {
        Map<String, User> resolvedByReference = new LinkedHashMap<>();
        if (references.isEmpty()) {
            return resolvedByReference;
        }

        Map<String, User> byId = userRepository.findAllByIdIn(references)
            .stream()
            .collect(Collectors.toMap(User::getId, Function.identity(), (first, second) -> first));
        Map<String, User> byUserName = userRepository.findAllByUserNameIn(references)
            .stream()
            .collect(Collectors.toMap(User::getUserName, Function.identity(), (first, second) -> first));
        Map<String, User> byEmail = userRepository.findAllByEmailIn(references)
            .stream()
            .collect(Collectors.toMap(User::getEmail, Function.identity(), (first, second) -> first));

        for (String reference : references) {
            User resolved = byId.get(reference);
            if (resolved == null) {
                resolved = byUserName.get(reference);
            }
            if (resolved == null) {
                resolved = byEmail.get(reference);
            }
            if (resolved != null) {
                resolvedByReference.put(reference, resolved);
            }
        }

        return resolvedByReference;
    }
}
