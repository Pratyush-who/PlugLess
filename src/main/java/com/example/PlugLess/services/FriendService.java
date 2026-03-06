package com.example.PlugLess.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.PlugLess.dto.friend.FriendRequestResponse;
import com.example.PlugLess.dto.user.UserResponse;
import com.example.PlugLess.entity.User;
import com.example.PlugLess.repository.UserRepository;

@Service
public class FriendService {

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

        // can't send to yourself
        if (sender.getId().equals(targetId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot send request to yourself");

        // already friends
        if (sender.getFriendIds().contains(targetId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already friends");

        // I already sent them a request — don't duplicate
        if (target.getFriendRequestIds().contains(sender.getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Friend request already sent");

        // target already sent ME a request — auto-accept instead of creating cross-requests
        if (sender.getFriendRequestIds().contains(targetId)) {
            sender.getFriendIds().add(targetId);
            target.getFriendIds().add(sender.getId());
            sender.getFriendRequestIds().remove(targetId);
            userRepository.save(sender);
            userRepository.save(target);
            return;
        }

        // normal case — add sender's ID to target's incoming requests
        target.getFriendRequestIds().add(sender.getId());
        userRepository.save(target);
    }

    public void acceptRequest(String myEmail, String requesterId) {
        User me = getByEmail(myEmail);
        User requester = getById(requesterId);

        if (!me.getFriendRequestIds().contains(requesterId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No friend request from this user");

        me.getFriendIds().add(requesterId);
        requester.getFriendIds().add(me.getId());
        me.getFriendRequestIds().remove(requesterId);

        userRepository.save(me);
        userRepository.save(requester);
    }

    public void rejectRequest(String myEmail, String requesterId) {
        User me = getByEmail(myEmail);

        if (!me.getFriendRequestIds().contains(requesterId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No friend request from this user");

        me.getFriendRequestIds().remove(requesterId);
        userRepository.save(me);
    }

    public void removeFriend(String myEmail, String friendId) {
        User me = getByEmail(myEmail);
        User friend = getById(friendId);

        if (!me.getFriendIds().contains(friendId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not in your friends list");

        me.getFriendIds().remove(friendId);
        friend.getFriendIds().remove(me.getId());

        userRepository.save(me);        // save both — both documents changed
        userRepository.save(friend);
    }

    public List<UserResponse> getMyFriends(String myEmail) {
        User me = getByEmail(myEmail);

        if (me.getFriendIds().isEmpty()) return List.of();

        return userRepository.findAllByIdIn(me.getFriendIds())
            .stream()
            .map(userService::toResponse)
            .collect(Collectors.toList());
    }

    public List<FriendRequestResponse> getIncomingRequests(String myEmail) {
        User me = getByEmail(myEmail);

        if (me.getFriendRequestIds().isEmpty()) return List.of();

        return userRepository.findAllByIdIn(me.getFriendRequestIds())
            .stream()
            .map(u -> new FriendRequestResponse(
                u.getId(),
                u.getUserName(),
                u.getDisplayName(),
                u.getProfileImageUrl(),
                u.isOnline(),
                u.getLastSeen()
            ))
            .collect(Collectors.toList());
    }
}
