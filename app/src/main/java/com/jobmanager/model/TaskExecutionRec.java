package com.jobmanager.model;

import java.time.Instant;

/**
 * Represents one execution record for a task.
 */
public class TaskExecutionRec {
    private Instant startTime;
    private Instant endTime;
    private String output;

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }

    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }
}
