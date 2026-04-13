package com.example.PlugLess.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.PlugLess.entity.PersonalDm;

@Repository
public interface PersonalDmRepository extends MongoRepository<PersonalDm, String> {
}

