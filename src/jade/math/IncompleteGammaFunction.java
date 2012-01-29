/*
 * IncompleteGammaFunction.java
 *
 * Created on August 19, 2005, 11:08 AM
 *
 * author: Stephen A. Smith
 */

package jade.math;

/**
 *
 * @author stephensmith
 */
public class IncompleteGammaFunction {
    
    /** Creates a new instance of IncompleteGammaFunction */
    public IncompleteGammaFunction(double alpha) {
        this.alpha = alpha;
        alphaLogGamma = GammaFunction.logGamma(alpha);
    }
    
    private double evaluateFraction(double x){
        if(fraction==null){
            fraction = new IncompleteGammaFunctionFraction(alpha);
            fraction.setDesiredPrecision(MachinePrecision.defaultNumericalPrecision());
        }
        fraction.setArgument(x);
        fraction.evaluate();
        return fraction.getResult();
    }
    
    private double evaluateSeries(double x){
        if(series==null){
            series = new IncompleteGammaFunctionSeries(alpha);
            series.setDesiredPrecision(MachinePrecision.defaultNumericalPrecision());
        }
        series.setArgument(x);
        series.evaluate();
        return series.getResult();
    }
    
    public double value(double x){
        if(x==0)
            return 0;
        double norm = Math.exp(Math.log(x)*alpha - x - alphaLogGamma);
        return x - 1 < alpha
                ? evaluateSeries(x)*norm
                : 1- norm / evaluateFraction(x);
    }
    
    private double alpha;
    private double alphaLogGamma;
    private IncompleteGammaFunctionSeries series;
    private IncompleteGammaFunctionFraction fraction;
}
