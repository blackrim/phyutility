/*
 * SequenceStateException.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.sequences;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id: SequenceStateException.java 185 2006-01-23 23:03:18Z rambaut $
 */
public class SequenceStateException extends Exception {
    public SequenceStateException(String s) {
        super(s);
    }
}
