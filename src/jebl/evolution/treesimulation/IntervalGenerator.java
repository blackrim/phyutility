/*
 * IntervalGenerator.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.treesimulation;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: IntervalGenerator.java 563 2006-12-07 17:43:10Z rambaut $
 */
public interface IntervalGenerator {
    double getInterval(double criticalValue, int lineageCount, double currentHeight);
}
