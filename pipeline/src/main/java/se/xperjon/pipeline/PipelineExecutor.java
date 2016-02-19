package se.xperjon.pipeline;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jon-Erik
 */
public class PipelineExecutor {

    private final Collection<Pipeline> pipelines;
    private final Map<Pipeline, Future<PipelineStatus>> pipelineMap;
    private final ExecutorService pool;
    private volatile boolean running = true;
    private static int threadSuffix;

    public PipelineExecutor(Collection<Pipeline> pipelines) {
        this.pipelines = pipelines;
        pool = Executors.newFixedThreadPool(pipelines.size(), (Runnable r) -> {
            Thread thread = new Thread(r);
            thread.setName("Pipline-" + threadSuffix++);
            return thread;
        });
        pipelineMap = new HashMap<>();
    }

    public void start() {
        Runnable r = () -> {
            startPipelines();
            while (running) {
                pipelineMap.forEach((pipeline, future) -> {
                    if (future.isDone()) {
                        try {
                            if (future.get().equals(PipelineStatus.ERROR())) {
                                System.out.println("Pipeline terminated with error");
                            }
                        } catch (InterruptedException | ExecutionException ex) {
                            Logger.getLogger(PipelineExecutor.class.getName()).log(Level.SEVERE, "Pipeline interrupted or exception thrown", ex);
                        }
                        pipelineMap.replace(pipeline, startPipeline(pipeline));
                    }
                });
            }
            System.out.println("Exiting pipeline executor");
        };
        Thread thread = new Thread(r);
        thread.setName("PipelineExecutor");
        thread.start();
    }

    public void stop() {
        running = false;
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    private Future<PipelineStatus> startPipeline(Pipeline pipeline) {
        return pool.submit(pipeline);
    }

    private void startPipelines() {
        pipelines.stream().forEach((pipeline) -> {
            Future<PipelineStatus> future = pool.submit(pipeline);
            pipelineMap.put(pipeline, future);
        });
    }

}
