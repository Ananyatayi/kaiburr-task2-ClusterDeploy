package com.jobmanager.controller;

import com.jobmanager.service.KubePodExecutor;
import com.jobmanager.model.TaskDoc;
import com.jobmanager.model.TaskExecutionRec;
import com.jobmanager.repo.TaskStore;
import com.jobmanager.service.CommandAudit;
import com.jobmanager.service.ShellRunner;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for Task 1 (unique implementation).
 * Endpoints:
 *  - GET  /v1/tasks            -> list all or single by ?id=
 *  - PUT  /v1/tasks            -> create/update a task (validate command)
 *  - DELETE /v1/tasks/{id}     -> delete by id
 *  - GET  /v1/tasks/find?name= -> search by name contains
 *  - PUT  /v1/tasks/{id}/execution -> run stored command, append TaskExecutionRec
 */
@RestController
@RequestMapping("/v1")
public class TaskApi {

    private final TaskStore store;
    private final CommandAudit audit;
    private final ShellRunner runner;
    private final KubePodExecutor kube;

    public TaskApi(TaskStore store, CommandAudit audit, ShellRunner runner, KubePodExecutor kube) {
        this.store = store;
        this.audit = audit;
        this.runner = runner;
        this.kube = kube;
    }

    // GET tasks (all or by id as query param)
    @GetMapping("/tasks")
    public ResponseEntity<?> get(@RequestParam(name = "id", required = false) String id) {
        if (id == null) return ResponseEntity.ok(store.findAll());
        return store.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("Task not found"));
    }

    // PUT a task (create or update)
    @PutMapping("/tasks")
    public ResponseEntity<?> upsert(@Valid @RequestBody TaskDoc t) {
        audit.verify(t.getCommand());
        if (t.getId() != null && t.getId().isBlank()) t.setId(null);
        return ResponseEntity.ok(store.save(t));
    }

    // DELETE a task by id
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        if (!store.existsById(id)) return ResponseEntity.status(404).body("Task not found");
        store.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // GET find by name contains
    @GetMapping("/tasks/find")
    public ResponseEntity<?> find(@RequestParam String name) {
        List<TaskDoc> result = store.findByNameContainingIgnoreCase(name);
        if (result.isEmpty()) return ResponseEntity.status(404).body("No tasks found");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/tasks/{id}/execution")
    public ResponseEntity<?> execute(@PathVariable String id) throws Exception {
        TaskDoc t = store.findById(id).orElse(null);
        if (t == null) return ResponseEntity.status(404).body("Task not found");

        audit.verify(t.getCommand());

        String mode = System.getenv().getOrDefault("EXEC_MODE", "local").toLowerCase();
        TaskExecutionRec run;
        if ("k8s".equals(mode)) {
            run = kube.run(t.getCommand());
        } else {
            run = runner.execute(t.getCommand());
        }

        t.getTaskExecutions().add(run);
        store.save(t);
        return ResponseEntity.ok(run);
    }
}
