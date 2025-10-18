package com.jobmanager.service;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Validates user-provided shell commands to avoid dangerous ones.
 */
@Component
public class CommandAudit {

    // block some risky keywords
    private static final Pattern DANGEROUS =
            Pattern.compile("(;|&&|\\|\\||rm\\s+-rf|shutdown|reboot|mkfs|:>|dd|curl\\s+http)", Pattern.CASE_INSENSITIVE);

    public void verify(String cmd) {
        if (cmd == null || cmd.isBlank())
            throw new IllegalArgumentException("Command cannot be empty");
        if (DANGEROUS.matcher(cmd).find())
            throw new IllegalArgumentException("Unsafe command detected: " + cmd);
    }
}
