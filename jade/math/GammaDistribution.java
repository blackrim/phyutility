/*
 * GammaDistribution.java
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
public class GammaDistribution implements ProbDistribution{
    
    /** Creates a new instance of GammaDistribution */
    public GammaDistribution(double shape, double scale){
        this.shape = shape;
        this.scale = scale;
        norm = Math.log( scale ) * shape + GammaFunction.logGamma( shape );
        if( shape < 1 )
            b = (Math.E + shape)/Math.E;
        else if( shape > 1){
            a = Math.sqrt(2 * shape -1);
            b = shape - Math.log(4.0);
            q = shape + 1 /a;
            d = 1+ Math.log(4.5);
        }
    }
    
    public void setShape(double shape){this.shape = shape;}
    public void setScale(double scale){this.scale = scale;}
    public double getShape(){return shape;}
    public double getScale(){return scale;}
    
    public double getValue(){
        double r;
        if(shape >1)
            r = randomForShapeGreaterThan1();
        else if(shape < 1)
            r = randomForShapeLessThan1();
        else
            r = randomForShapeEqualTo1();
        return r * scale;
    }
    
    private double randomForShapeEqualTo1(){
        return -Math.log( 1 - r.nextDouble());
    }
    private double randomForShapeGreaterThan1(){
        double u1, u2, v, y, z, w;
        while (true){
            u1 = r.nextDouble();
            u2 = r.nextDouble();
            v = a*Math.log(u1/(1-u1));
            y = shape * Math.exp(v);
            z = u1*u1*u2;
            w = b+q*v-y;
            if(w+d-4.5*z>=0||w>=Math.log(z))
                return y;
            
        }
    }
    private double randomForShapeLessThan1(){
        double p,y;
        while(true){
            p = r.nextDouble()*b;
            if(p>1){
                y = -Math.log((b-p)/shape);
                if(r.nextDouble()<=Math.pow(y, shape-1))
                    return y;
            }
            y = Math.pow(p, 1/shape);
            if(r.nextDouble()<=Math.exp(-y))
                return y;                
        }
    }
    
    public double getPDF(double x){
        return incompleteGammaFunction().value(x/scale);
    }
    
    private IncompleteGammaFunction incompleteGammaFunction(){
        if(incompleteGammaFunction==null){
            incompleteGammaFunction = new IncompleteGammaFunction(shape);
        }
        return incompleteGammaFunction;
    }
    
    //private methods
    private double shape;
    private double scale;
    private double norm;
    //private random number variables
    private double a;
    private double b;
    private double q;
    private double d;
    private Random r = new Random();
    private IncompleteGammaFunction incompleteGammaFunction;
}
