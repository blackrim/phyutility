/*
 * GammaFunction.java
 *
 * Created on August 9, 2005, 4:01 PM
 *
 * author: Stephen A. Smith
 *
 *
 * ideas from Didier H. Besset
 */

package jade.math;

/**
 *
 * @author stephensmith
 */
public final class GammaFunction {
    
    /** Creates a new instance of GammaFunction */
    public GammaFunction() {
    }
    public static double beta( double x, double y ){
        return Math.exp( logGamma( x ) + logGamma( y ) - logGamma( x + y ));
    }
    public static long factorial(long n){
        return n<2 ? 1: n * factorial(n -1);
    }
    public static double gamma(double x){
        return  x > 1
                    ? Math.exp(leadingFactor(x)) * series(x) * sqrt2PI / x
                    : (x > 0 ? gamma(x+1)/x
                                        : Double.NaN);
    }
    private static double leadingFactor(double x){
        double temp = x+5.5;
        return Math.log(temp)*(x+0.5)-temp;
    }
    public static double logBeta( double x, double y ){
        return logGamma( x ) + logGamma( y ) - logGamma( x + y );
    }
    public static double logGamma(double x){
        return x > 1
                   ? leadingFactor(x) + Math.log( series(x) * sqrt2PI/x)
                   : (x > 0 ? logGamma(x+1) - Math.log(x)
                                            : Double.NaN);
    }
    private static double series(double x){
        double answer  = 1.000000000190015;
        double term = x;
        for(int i=0;i<6;i++){
            term +=1;
            answer+=coefficients[i]/term;
        }
        return answer;
    }
    
    static double sqrt2PI = Math.sqrt( 2 * Math.PI );
    static double [] coefficients = { 76.18009172947146,
                                      -86.505320329411677,
                                      24.01409824083091,
                                      -1.231739572450155,
                                      0.1208650973866179e-2,
                                      -0.5395239384953e-5};
}
