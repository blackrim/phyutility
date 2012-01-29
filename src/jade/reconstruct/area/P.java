/*
 * P.java
 *
 * Created on April 11, 2005, 9:30 PM
 */

package jade.reconstruct.area;


/**
 *
 * @author stephensmith
 */
public class P {
    
    /** Creates a new instance of P */
    public P(Q qin) {
        q=qin;
        me = new MatrixExponential(q.getQ().length);
        me.updateByRelativeRates(q.getQ());
    }
    
    public double getRateChangeProbability(int row, int column){
        return me.getTransitionProbability(row,column);
    }
    
    public void setBL(double b){
        me.setDistance(b);
    }
    
    //private double [][]arr;
    //private double bl;
    private Q q;
    private MatrixExponential me;
}
