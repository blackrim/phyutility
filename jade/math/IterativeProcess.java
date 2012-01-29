/*
 * IterativeProcess.java
 *
 * Created on August 19, 2005, 11:30 AM
 *
 * author: Stephen A. Smith
 */

package jade.math;

/**
 *
 * @author stephensmith
 */
public abstract class IterativeProcess {
    
    /** Creates a new instance of IterativeProcess */
    public IterativeProcess() {
    }
    
    public void evaluate(){
        iterations = 0;
        initializeIterations();
        while (iterations++<maximumIterations){
            precision = evaluateIteration();
            if(hasConverged())
                break;
        }
        finalizeIterations();
    }
    abstract public double evaluateIteration();
    public void finalizeIterations(){
        
    }
    public double getDesiredPrecision(){ return desiredPrecision; }
    public int getIterations(){ return iterations; }
    public int getMaximumIterations(){ return maximumIterations; }
    public double getPrecision(){ return precision; }
    public boolean hasConverged(){ return precision < desiredPrecision; }
    public void initializeIterations(){ }
    public double relativePrecision(double epsilon, double x){
        return x > MachinePrecision.defaultNumericalPrecision()
        ? epsilon / x:epsilon;
    }
    public void setDesiredPrecision(double prec) throws IllegalArgumentException{
        if(prec<=0)
            throw new IllegalArgumentException("non-positive precision: "+prec);
        desiredPrecision = prec;
    }
    public void setMaximumIterations(int maxIter) throws IllegalArgumentException{
        if(maxIter<=0)
            throw new IllegalArgumentException("non-positive max iters: "+maxIter);
        maximumIterations = maxIter;
    }
    
    
    private int iterations;
    private int maximumIterations = 50;
    private double desiredPrecision = MachinePrecision.defaultNumericalPrecision();
    private double precision;
}
