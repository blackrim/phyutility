// TransitionPenaltyTable.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package jade.data;


/**
 * Implements a table of transition penalties for a particular datatype.
 * Used for alignment scoring. 
 *
 * @version $Id: TransitionPenaltyTable.java,v 1.2 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 */
public interface TransitionPenaltyTable  {
    
	double penalty(int a, int b);
	DataType getDataType();
}
