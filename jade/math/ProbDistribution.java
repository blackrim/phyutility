/*
 * ProbDistribution.java
 *
 * Created on August 9, 2005, 3:09 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package jade.math;

/**
 *
 * @author stephensmith
 */
public interface ProbDistribution {
    /**
     *@return double value
     */
    public double getValue();
    
    /**
     *@return the integral of the probability density function from 0 to x
     */
    public double getPDF(double x);
}
