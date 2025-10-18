package com.jobmanager.service;

import com.jobmanager.model.TaskExecutionRec;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;

/**
 * Executes a shell command locally and captures stdout.
 */
@Component
public class ShellRunner {

    public TaskExecutionRec execute(String command) throws Exception {
        TaskExecutionRec rec = new TaskExecutionRec();
        rec.setStartTime(Instant.now());

        ProcessBuilder pb = new ProcessBuilder();
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            pb.command("cmd.exe", "/c", command);
        } else {
            pb.command("bash", "-lc", command);
        }

        Process p = pb.start();

        StringBuilder out = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                out.append(line).append(System.lineSeparator());
            }
        }

        p.waitFor(); // wait for process to finish
        rec.setEndTime(Instant.now());
        rec.setOutput(out.toString().trim());
        return rec;
    }
}
