package jebl.evolution.io;

import jebl.evolution.alignments.Alignment;

import java.io.IOException;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id: AlignmentExporter.java 540 2006-11-23 19:59:23Z pepster $
 */
public interface AlignmentExporter {

	/**
	 * export one alignment.
     * @param alignment  to export
     * @throws java.io.IOException
     */
	void exportAlignment(Alignment alignment) throws IOException;
}
