package jebl.evolution.align;

/**
 *
 * @author Alexei Drummond
 *
 * @version $Id: TracebackPlotter.java 185 2006-01-23 23:03:18Z rambaut $
 *
 */
public interface TracebackPlotter {

    void newTraceBack(String sequence1, String sequence2);

    void traceBack(Traceback t);

    void finishedTraceBack();   
}
