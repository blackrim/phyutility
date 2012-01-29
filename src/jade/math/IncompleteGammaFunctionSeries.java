/*
 * IncompleteGammaFunctionSeries.java
 *
 * Created on August 19, 2005, 11:21 AM
 *
 * author: Stephen A. Smith
 */

package jade.math;

/**
 *
 * @author stephensmith
 */
public class IncompleteGammaFunctionSeries extends InfiniteSeries{
    
    /** Creates a new instance of IncompleteGammaFunctionSeries */
    public IncompleteGammaFunctionSeries(double x) {
        alpha = x;
    }
    
    protected void computeTermAt(int n){
        sum += 1;
        lastTerm *= x/sum;
        return;
    }
    
    protected double initialValue(){
        lastTerm = 1/alpha;
        sum = alpha;
        return lastTerm;
    }
    
    private double alpha;
    private double sum;
}
