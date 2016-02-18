package se.xperjon.pipeline;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jon-Erik
 */
public class Main {

    private final Collection<Pipeline> pipelines;
    private final Map<Pipeline,Future<PipelineStatus>> pipelineMap;
    private final ExecutorService pool;

    public Main(Collection<Pipeline> pipelines) {
        this.pipelines = pipelines;
        pool = Executors.newFixedThreadPool(pipelines.size());
        pipelineMap = new HashMap<>();
    }

    public void start() {
        startPipelines();
        while(true) {
            pipelineMap.forEach((pipeline,future) -> {
                if(future.isDone()) {
                    try {
                        if(future.get().equals(PipelineStatus.ERROR())) {
                            System.out.println("Pipeline terminated with error");
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Pipeline interrupted or exception thrown", ex);
                    }
                    pipelineMap.replace(pipeline, startPipeline(pipeline));
                }
            });
            try {
                System.out.println("sleeping");
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private Future<PipelineStatus> startPipeline(Pipeline pipeline) {
        return pool.submit(pipeline);
    }
    private void startPipelines(){
        pipelines.stream().forEach((pipeline) -> {
            Future<PipelineStatus> future = pool.submit(pipeline);
            pipelineMap.put(pipeline,future);
        });
    }
    
}
