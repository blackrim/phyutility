/*
 * InfiniteSeries.java
 *
 * Created on August 19, 2005, 11:24 AM
 *
 * author: Stephen A. Smith
 */

package jade.math;

/**
 *
 * @author stephensmith
 */
public abstract class InfiniteSeries extends IterativeProcess{
    
    /** Creates a new instance of InfiniteSeries */
    public InfiniteSeries() {
    }
    
    protected abstract void computeTermAt(int n);
    public double evaluateIteration(){
        computeTermAt(getIterations());
        result+=lastTerm;
        return relativePrecision(Math.abs(lastTerm), Math.abs(result));
    }
    public double getResult(){
        return result;
    }
    public void initializeIterations(){
        result = initialValue();
    }
    protected abstract double initialValue();
    public void setArgument(double r){
        x = r;
        return;
    }
    
    private double result;
    protected double x;
    protected double lastTerm;
}
