/*
 * Q.java
 *
 * Created on April 11, 2005, 9:30 PM
 */

package jade.reconstruct.area;


/**
 *
 * @author stephensmith
 */
public class Q {
    
    /** Creates a new instance of Q */
    public Q(double [][] inr) {
        arr=inr;//
        //doSum();
        //scale();
        setI();//
        
    }
    //do before scale
    private void doSum(){
        sum=0;
        for(int i=0;i<arr.length;i++){
            for(int j=0;j<arr.length;j++){
                if(i==j){                    
                }else{
                    sum=sum+arr[i][j];
                }
            }
        }
        scale = 1*sum;
        //System.out.println("sum "+sum+" scale "+scale);
    }
    //check divide multiply
    private void scale(){
        for(int i=0;i<arr.length;i++){
            for(int j=0;j<arr.length;j++){
                arr[i][j]=arr[i][j]/scale;
            }
        }
        setI();
    }
    
    private void setI(){
        double [] rows = new double [arr.length];
        for(int i=0;i<arr.length;i++){
            rows[i]=0;
            for(int j=0;j<arr.length;j++){
                if(i!=j){
                    rows[i]=rows[i]+arr[i][j];
                }
            }
        }
        for(int i=0;i<arr.length;i++){
            for(int j=0;j<arr.length;j++){
                if(i==j){
                    arr[i][j]=0-rows[i];
                }
            }
        }
        //
        //added dont know
        for(int i=0;i<arr.length;i++){
            for(int j=0;j<arr.length;j++){
                arr[i][j]=arr[i][j];
            }
        }
        //
        //
    }
    
    public double [][] getQ(){
        return arr;
    }
    
    public void printQ(){
        for(int i=0;i<arr.length;i++){
            for(int j=0;j<arr.length;j++){
                System.out.print(arr[i][j]+" ");
            }
            System.out.println();
        }
    }
    
    //private int dimensions;
    private double [][]arr;
    private double sum;
    private double scale;
}
