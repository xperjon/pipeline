package se.xperjon.pipeline;

import java.util.Objects;

/**
 *
 * @author Jon-Erik
 */
public class PipelineStatus {

    private final Status status;
    private PipelineStatus(Status status) {
        this.status = status;
    }
    
    public static PipelineStatus OK() {
        return new PipelineStatus(Status.OK);
    }
    
    public static PipelineStatus ERROR() {
        return new PipelineStatus(Status.ERROR);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.status);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PipelineStatus other = (PipelineStatus) obj;
        if (this.status != other.status) {
            return false;
        }
        return true;
    }
    
    enum Status {
        OK,
        ERROR
    }
}
