/*
 * ContinuedFraction.java
 *
 * Created on August 19, 2005, 12:31 PM
 *
 * author: Stephen A. Smith
 */

package jade.math;

/**
 *
 * @author stephensmith
 */
public abstract class ContinuedFraction extends IterativeProcess{
    
    /** Creates a new instance of ContinuedFraction */
    public ContinuedFraction() {
    }
    
    protected abstract void computeFactorsAt(int n);
    
    public double evaluateIteration(){
        computeFactorsAt(getIterations());
        denominator = 1/limitedSmallValue(factors[0]*denominator+factors[1]);
        numerator = limitedSmallValue(factors[0]/numerator+factors[1]);
        double delta = numerator * denominator;
        result *=delta;
        return Math.abs(delta-1);
    }
    
    public double getResult(){ return result; }
    
    public void initializeIterations(){
        numerator = limitedSmallValue(initialValue());
        denominator = 0;
        result = numerator;
        return;
    }
    
    protected abstract double initialValue();
    
    private double limitedSmallValue(double r){
        return Math.abs(r)<MachinePrecision.smallNumber()
        ? MachinePrecision.smallNumber(): r;
    }
    
    public void setArgument(double r){
        x = r;
        return;
    }
    
    private double result;
    protected double x;
    private double numerator;
    private double denominator;
    protected double [] factors = new double [2];
}
