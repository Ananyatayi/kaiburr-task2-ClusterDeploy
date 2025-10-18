package com.jobmanager.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Mongo document that matches the assignment schema:
 * id, name, owner, command, taskExecutions
 */
@Document("tasks")
public class TaskDoc {

    @Id
    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private String owner;

    @NotBlank
    private String command;

    private List<TaskExecutionRec> taskExecutions = new ArrayList<>();

    // Getters / Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public List<TaskExecutionRec> getTaskExecutions() { return taskExecutions; }
    public void setTaskExecutions(List<TaskExecutionRec> taskExecutions) { this.taskExecutions = taskExecutions; }
}
