package com.jobmanager.repo;

import com.jobmanager.model.TaskDoc;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TaskStore extends MongoRepository<TaskDoc, String> {
    List<TaskDoc> findByNameContainingIgnoreCase(String name);
}
