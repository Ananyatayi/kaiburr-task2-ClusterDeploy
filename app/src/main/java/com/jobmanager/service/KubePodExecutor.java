package com.jobmanager.service;

import com.jobmanager.model.TaskExecutionRec;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class KubePodExecutor {

    private static final String NAMESPACE =
            System.getenv().getOrDefault("JOBMGR_NAMESPACE", "jobmgr");

    /**
     * Run command in a short-lived BusyBox pod and return captured output.
     * Uses pod logs as the command output.
     */
    public TaskExecutionRec run(String command) throws Exception {
        try (KubernetesClient client = new KubernetesClientBuilder().build()) {
            String podName = "jobrun-" + UUID.randomUUID().toString().substring(0, 8);

            // BusyBox containers accept ["sh","-lc", "<cmd>"]
            Pod pod = new PodBuilder()
                    .withNewMetadata()
                    .withName(podName)
                    .withNamespace(NAMESPACE)
                    .addToLabels("app", "jobmgr-exec")
                    .endMetadata()
                    .withNewSpec()
                    .addToContainers(new ContainerBuilder()
                            .withName("runner")
                            .withImage("busybox:1.36")
                            .withCommand("sh", "-lc", command)
                            .build())
                    .withRestartPolicy("Never")
                    .endSpec()
                    .build();

            TaskExecutionRec rec = new TaskExecutionRec();
            rec.setStartTime(Instant.now());

            client.pods().inNamespace(NAMESPACE).resource(pod).create();

            // Wait until it finishes (Succeeded/Failed)
            client.pods().inNamespace(NAMESPACE).withName(podName)
                    .waitUntilCondition(
                            p -> p != null && p.getStatus() != null
                                    && p.getStatus().getPhase() != null
                                    && (p.getStatus().getPhase().equals("Succeeded")
                                    || p.getStatus().getPhase().equals("Failed")),
                            120, TimeUnit.SECONDS);

            // Read logs as command output
            String logs = client.pods().inNamespace(NAMESPACE).withName(podName).getLog(true);
            rec.setEndTime(Instant.now());
            rec.setOutput(logs == null ? "" : logs.trim());

            // Clean up pod (optional but tidy)
            client.pods().inNamespace(NAMESPACE).withName(podName).delete();

            return rec;
        }
    }
}
