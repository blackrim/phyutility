/*
 * NormalDistribution.java
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
public class NormalDistribution implements ProbDistribution{
    
    /** Creates a new instance of NormalDistribution */
    public NormalDistribution() {
    }
    
    public NormalDistribution(double mean, double stdev){
        this.mean = mean;
        this.stdev = stdev;
    }
    
    public void setStDev(double stdev){ this.stdev = stdev; }
    
    public void setMean(double mean){ this.mean = mean; }
    
    public double getValue(){
        return (r.nextGaussian()*stdev)+mean;
    }
    
    public double getPDF(double x){
        //add error function
        return (x-mean)/stdev;
    }
    
    //private methods
    private double stdev;
    private double mean;
    private Random r = new Random();
}
