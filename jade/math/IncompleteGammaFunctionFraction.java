/*
 * IncompleteGammaFunctionFraction.java
 *
 * Created on August 19, 2005, 12:27 PM
 *
 * author: Stephen A. Smith
 */

package jade.math;

/**
 *
 * @author stephensmith
 */
public class IncompleteGammaFunctionFraction extends ContinuedFraction{
    
    /** Creates a new instance of IncompleteGammaFunctionFraction */
    public IncompleteGammaFunctionFraction(double a) {
        alpha = a;
    }
    
    protected void computeFactorsAt(int n){
        sum+=2;
        factors[0] = (alpha-n)*n;
        factors[1] = sum;
        return;
    }
    
    protected double initialValue(){
        sum = x-alpha+1;
        return sum;
    }
    
    private double alpha;
    private double sum;
}
