package jebl.evolution.distances;

/**
 * @author Matthew Cheung
 * @version $Id$
 */
public class CannotBuildDistanceMatrixException extends IllegalArgumentException {
    public CannotBuildDistanceMatrixException(String msg){
        super(msg);
    }
}
