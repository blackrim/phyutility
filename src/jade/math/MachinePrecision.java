/*
 * MachinePrecision.java
 *
 * Created on August 19, 2005, 11:17 AM
 *
 * author: Stephen A. Smith
 */

package jade.math;

/**
 *
 * @author stephensmith
 */
public final class MachinePrecision {
      
    private static void computeLargestNumber(){
        double floatingRadix = getRadix();
        double fullMantissaNumber = 1.0d - floatingRadix * getNegativeMachinePrecision();
        while(!Double.isInfinite(fullMantissaNumber)){
            largestNumber = fullMantissaNumber;
            fullMantissaNumber *= floatingRadix; 
        }
    }
    
    private static void computeMachinePrecision(){
        double floatingRadix = getRadix();
        double inverseRadix = 1.0d/floatingRadix;
        machinePrecision = 1.0d;
        double tmp = 1.0d + machinePrecision;
        while(tmp - 1.0d != 0.0d){
            machinePrecision *= inverseRadix;
            tmp= 1.0d+machinePrecision;
        }
    }
    
    private static void computeNegativeMachinePrecision(){
        double floatingRadix = getRadix();
        double inverseRadix = 1.0d / floatingRadix;
        negativeMachinePrecision = 1.0d;
        double tmp = 1.0d + negativeMachinePrecision;
        while(tmp - 1.0d != 0.0d){
            negativeMachinePrecision *= inverseRadix;
            tmp= 1.0d-negativeMachinePrecision;
        }
    }
    
    private static void computeRadix(){
        double a = 1.0d;
        double tmp1,tmp2;
        do{ a +=a;
            tmp1 = a +1.0d;
            tmp2 = tmp1 -a;
        }while(tmp2-1.0d!=0.0d);
        double b = 1.0d;
        while(radix==0){
            b+=b;
            tmp1 = a+b;
            radix = (int)(tmp1-a);
        }
    }
    
    private static void computeSmallestNumber(){
        double floatingRadix = getRadix();
        double inverseRadix = 1.0d/ floatingRadix;
        double fullMantissaNumber = 1.0d - floatingRadix * getNegativeMachinePrecision();
        while(fullMantissaNumber!=0.0d){
            smallestNumber = fullMantissaNumber;
            fullMantissaNumber *= inverseRadix;
        }
    }
    
    public static double defaultNumericalPrecision(){
        if(defaultNumericalPrecision == 0){
            defaultNumericalPrecision = Math.sqrt(getMachinePrecision());
        }return defaultNumericalPrecision;
    }
    
    public static boolean equal(double a, double b){
        return equal(a, b, defaultNumericalPrecision());
    }
    
    public static boolean equal(double a, double b, double prec){
        double norm = Math.max(Math.abs(a), Math.abs(b));
        return norm < prec || Math.abs(a-b) < prec *norm;
    }
    
    public static double getLargestExponentialArgument(){
        if(largestExponentialArgument == 0)
            largestExponentialArgument = Math.log(getLargestNumber());
        return largestExponentialArgument;
    }
    
    public static double getLargestNumber(){
        if(largestNumber == 0)
            computeLargestNumber();
        return largestNumber;
    }
    
    public static double getMachinePrecision(){
        if(machinePrecision == 0)
            computeMachinePrecision();
        return machinePrecision;
    }
    
    public static double getNegativeMachinePrecision(){
        if(negativeMachinePrecision == 0)
            computeNegativeMachinePrecision();
        return negativeMachinePrecision;
    }
    
    public static int getRadix(){
        if(radix==0)
            computeRadix();
        return radix;
    }
    
    public static double getSmallestNumber(){
        if(smallestNumber == 0)
            computeSmallestNumber();
        return smallestNumber;
    }
    
    public static double smallNumber(){
        if(smallNumber==0)
            smallNumber = Math.sqrt(getSmallestNumber());
        return smallNumber;
    }
    
    static private double defaultNumericalPrecision = 0;
    static private double smallNumber = 0;
    static private int radix = 0;
    static private double machinePrecision = 0;
    static private double negativeMachinePrecision = 0;
    static private double smallestNumber = 0;
    static private double largestNumber = 0;
    static private double largestExponentialArgument = 0;
    private static final double scales [] = {1.25, 2, 2.5, 4, 5, 7.5, 8, 10};
    private static final double semiIntegerScales[] = {2, 2.5, 4, 5, 7.5, 8, 10};
    private static final double integerScales[] = {2,4,5,8,10};
}
