package jebl.evolution.io;

import jebl.evolution.distances.BasicDistanceMatrix;
import jebl.evolution.distances.DistanceMatrix;
import jebl.evolution.taxa.Taxon;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: TabDelimitedImporter.java 185 2006-01-23 23:03:18Z rambaut $
 */
public class TabDelimitedImporter implements DistanceMatrixImporter {

    /**
     * Constructor
     */
    public TabDelimitedImporter(Reader reader, Triangle triangle, boolean diagonal, boolean rowLabels, boolean columnLabels) {
        helper = new ImportHelper(reader);
        this.triangle = triangle;
        this.diagonal = diagonal;
        this.rowLabels = rowLabels;
        this.columnLabels = columnLabels;

        if (!rowLabels && !columnLabels) {
            throw new IllegalArgumentException("The matrix must have either row labels or column labels (or both)");
        }
    }

    /**
     * importDistances.
     */
    public List<DistanceMatrix> importDistanceMatrices() throws IOException, ImportException {
        List<Taxon> taxa = new ArrayList<Taxon>();
        List<List<Double>> rows = new ArrayList<List<Double>>();

        boolean done = false;

        if (columnLabels) {
            String line = helper.readLine();
            String[] labels = line.split("\t");
            for (String label : labels) {
                Taxon taxon = Taxon.getTaxon(label);
                if (taxa.contains(taxon)) {
                    throw new ImportException.BadFormatException("The taxon label, " + taxon.getName() + ", appears more than once in the matrix");
                }

                taxa.add(taxon);
            }
        }


        do {
            try {
                helper.skipWhile(" ");

                String line = helper.readLine();
                String[] tokens = line.split("\t");

                int i = 0;
                if (rowLabels) {
                    Taxon taxon = Taxon.getTaxon(tokens[0]);
                    i++;

                    if (columnLabels) {
                        int index = taxa.indexOf(taxon);
                        if (index != i) {
                            throw new ImportException.BadFormatException("The row label, " + taxon.getName() + ", is missing or in a different order from the column labels");
                        }
                    } else {
                        if (taxa.contains(taxon)) {
                            throw new ImportException.BadFormatException("The taxon label, " + taxon.getName() + ", appears more than once in the matrix");
                        }

                        taxa.add(taxon);
                    }
                }

                List<Double> row = new ArrayList<Double>();
                for (int j = i; j < tokens.length; j++) {
                    row.add(Double.parseDouble(tokens[j]));
                }

                rows.add(row);

            } catch( EOFException eofe ) {
                done = true;
            }

        } while (!done);

        double[][] distances = new double[rows.size()][rows.size()];

        int i = 0;
        for (List<Double> row : rows) {

            if (i >= distances.length) {
                throw new ImportException.BadFormatException("Too many rows in matrix");
            }
            if (triangle == Triangle.LOWER) {
                int j = 0;
                for (Double distance : row) {
                    if (j >= distances[i].length) {
                        throw new ImportException.BadFormatException("Too many values in row " + Integer.toString(i+1) + " of matrix");
                    }
                    if (i != j) {
                        distances[i][j] = distance.doubleValue();
                        distances[j][i] = distance.doubleValue();
                    } else {
                        if (diagonal) {
                            distances[i][j] = distance.doubleValue();
                        }
                    }
                    j++;
                }
            } else if (triangle == Triangle.UPPER) {
                int j = i;
                for (Double distance : row) {
                    if (j >= distances[i].length) {
                        throw new ImportException.BadFormatException("Too many values in row " + Integer.toString(i+1) + " of matrix");
                    }
                    if (i != j) {
                        distances[i][j] = distance.doubleValue();
                        distances[j][i] = distance.doubleValue();
                    } else {
                        if (diagonal) {
                            distances[i][j] = helper.readDouble();
                        }
                    }
                    j++;
                }
            } else {
                int j = 0;
                for (Double distance : row) {
                    if (j >= distances[i].length) {
                        throw new ImportException.BadFormatException("Too many values in row " + Integer.toString(i+1) + " of matrix");
                    }
                    if (i != j || diagonal) {
                        distances[i][j] = distance.doubleValue();
                    } else {
                        distances[i][j] = 0.0;
                    }
                    j++;
                }
            }
            i++;
        }

        List<DistanceMatrix> matrices = new ArrayList<DistanceMatrix>();
        matrices.add(new BasicDistanceMatrix(taxa, distances));
        return matrices;
    }

    private final ImportHelper helper;
    private final Triangle triangle;
    private final boolean diagonal;
    private final boolean rowLabels;
    private final boolean columnLabels;
}
