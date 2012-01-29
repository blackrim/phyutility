package jebl.evolution.distances;

import jebl.evolution.taxa.Taxon;

import java.util.Collection;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: DistanceMatrix.java 186 2006-01-24 00:41:22Z pepster $
 */
public interface DistanceMatrix {

    /**
     * Gets the size of the matrix (which is square), i.e., number of rows or columns.
     * @return the size
     */
    int getSize();

    /**
     * @return the list of taxa that the state values correspond to.
     */
    List<Taxon> getTaxa();

    /**
     * Gets the distance at a particular row and column
     * @param row the row index
     * @param column the column index
     * @return the distance
     */
    double getDistance(int row, int column);

    /**
     * Gets the distance between 2 taxa
     * @param taxonRow
     * @param taxonColumn
     * @return the distance
     */
    double getDistance(Taxon taxonRow, Taxon taxonColumn);

    /**
     * Gets a sub-matrix for only those taxa in the collection (all
     * of which should be present in this matrix).
     * @param taxa
     * @return the new submatrix
     */
    DistanceMatrix getSubmatrix(Collection<Taxon> taxa);

    /**
     * Gets a 2-dimensional array containing the distances
     * @return the distances
     */
    double[][] getDistances();
}