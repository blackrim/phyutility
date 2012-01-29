/*
 * ExponentialDistribution.java
 *
 * Created on August 9, 2005, 3:24 PM
 *
 * author: Stephen A. Smith
 */

package jade.math;

import java.util.*;
/**
 *
 * @author stephensmith
 */
public class ExponentialDistribution implements ProbDistribution{
    
    /** Creates a new instance of ExponentialDistribution */
    public ExponentialDistribution() {
    }
    public ExponentialDistribution( double beta ){
        if(beta<=0)
            throw new IllegalArgumentException(" Exponential fall-off must be positive ( function was sent a negative value ) ");
        else
            this.beta = beta;
    }
    /**
     *@param beta the falloff value for the exponential distribution
     */
    public void setFallOff( double beta ){
        this.beta = beta;
    }
    
    public double getFallOff(){ return beta; }    
    
    public double getValue(){
        return -beta * Math.log(r.nextDouble());
    }
    
    //
    //probability of finding something smaller than x
    //@return the intregral of the probability distribution function from 0 to x
    public double getPDF(double x){
        return (1/beta)*Math.exp(-(1/beta)*x);
    }
    private double beta;
    private Random r = new Random();
}
