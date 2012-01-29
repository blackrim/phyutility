/*
 * BetaDistribution.java
 *
 * Created on August 9, 2005, 3:58 PM
 *
 * author: Stephen A. Smith
 */

package jade.math;

/**
 *
 * @author stephensmith
 */
public class BetaDistribution implements ProbDistribution{
    
    /** Creates a new instance of BetaDistribution */
    public BetaDistribution( double shape1, double shape2) {
        alpha1 = shape1;
        alpha2 = shape2;
        norm = GammaFunction.logBeta(alpha1, alpha2);
        gamma1 = new GammaDistribution( alpha1, 1.0 );
        gamma2 = new GammaDistribution( alpha2, 1.0 );
    }
    
    public void setShape1(double shape1){
        alpha1 = shape1;
    }
    public void setShape2(double shape2){
        alpha2 = shape2;
    }
    public double getShape1(){ return alpha1; }
    public double getShape2(){ return alpha2; }
    public double getValue(){
        double y1 = gamma1.getValue();
        double y2 = gamma2.getValue();
        return y1 / (y1 + y2);
    }
    
    public double getPDF(double x){
        return x;
    }
    
    //private methods
    private double alpha1;
    private double alpha2;
    private double norm;
    private GammaDistribution gamma1;
    private GammaDistribution gamma2;
    
}
