/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.xperjon.pipeline;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jon-Erik
 */
public class NewEmptyJUnitTest {

    public NewEmptyJUnitTest() {
    }

    @Test
    public void twoPipelines() {
        Pipeline pipeline1 = () -> {
            System.out.println("starting 1");
            return PipelineStatus.OK();
        };
        Pipeline pipeline2 = () -> {
            System.out.println("starting 2");
            return PipelineStatus.ERROR();
        };
        PipelineExecutor main = new PipelineExecutor(Arrays.asList(pipeline1, pipeline2));
//        main.start();
    }

    @Test
    public void pipelineThrowingException() {
        Pipeline pipeline1 = () -> {
            System.out.println("starting 1");
            throw new IllegalArgumentException("error");
        };
        PipelineExecutor main = new PipelineExecutor(Arrays.asList(pipeline1));
//        main.start();
    }

    @Test
    public void longRunningPipeline() {
        Pipeline p1 = () -> {
            try {
                System.out.println("Starting long running pipeline");
                Thread.sleep(10000);
//                throw new RuntimeException("Error");
            } catch (InterruptedException ex) {
                Logger.getLogger(PipelineExecutor.class.getName()).log(Level.SEVERE, null, ex);
            }
            return PipelineStatus.OK();
        };
        Pipeline p2 = () -> {
            System.out.println("starting 2");
            Thread.sleep(1000);
            return PipelineStatus.OK();
        };
        PipelineExecutor main = new PipelineExecutor(Arrays.asList(p2,p1));
        main.start();
        
        try {
            Thread.sleep(25000);
        } catch (InterruptedException ex) {
            Logger.getLogger(NewEmptyJUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Exiting");
        main.stop();
    }
}
