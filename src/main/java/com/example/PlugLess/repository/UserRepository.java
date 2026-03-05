package com.example.PlugLess.repository;

import java.util.List;
import java.util.Optional;

import com.example.PlugLess.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUserName(String userName);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUserName(String userName);
    List<User> findAllByIsOnline(boolean isOnline);
    long countByIsOnline(boolean isOnline);
}
