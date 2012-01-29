/*
 * NChooseM.java
 *
 * Created on June 26, 2005, 12:19 PM
 * 
 *  Stephen Smith 
 */

package jade.math;

/**
 *
 * @author stephensmith
 */
public class NChooseM {
    
    /** Creates a new instance of NChooseM */
    public NChooseM() {
    }    
    public static int combinations(int m, int n){
        if(!(m >= n)&&!(n >= 0))
            System.out.println("m >= n >= 0 required");
        if (n > (m >> 1))
            n = m-n;
        if (n == 0)
            return 1;
        int result = m;
        int i=2;
        m=m-1;n=n-1;
        while(n>0){
            //assert (result * m) % i == 0
            result = result * m / i;
            i = i+1;
            n = n-1;
            m = m-1;
        }
        return result;
    }
    public static int [] combinationsAtIndex(int m, int n, double i){
        if(!(m >= n)&&!(n >= 1))
            System.out.println("m >= n >= 1 required");
        double c=combinations(m,n);
        if(!(0 <= i)&&!(i < c))
            System.out.println("0 <= i < comb(m,n) required");
        int [] result = new int [0];
        c = c * n / m;
        for(int j=0;j<=m;j++){
            if (i < c){
                result = Utils.addToArray(result,j);
                n = n-1;
                if (n == 0)
                    break;
                c = c * n / (m-1);
            }
            else{
                i = i-c;
                c = c * (m-n) / (m-1);
            }
            m = m-1;
        }
        //assert i == 0
        return result;
    }
    public static int [][] iterate(int M, int N){
        if(!(M >= N)&&!(N >= 1))
            System.out.println("M >= N >= 1 required");
        int ncombs = combinations(M,N);
        int [][] result = new int[ncombs][0];
        for(int x=0;x<ncombs;x++){
            int i = x; int n = N; int m = M;
            int c = ncombs * n / m;
            int element=0;
            while(m>0){
                //System.out.println(element+" "+i+" "+c+" "+m+" "+n);
                if (i < c){
                    result[x] = Utils.addToArray(result[x], element);
                    n = n-1;
                    if (n == 0)
                        break;
                    c = c*n/(m-1);
                }
                else{
                    i = i-c;
                    c = c*(m-n)/(m-1);
                }
                element++;
                m = m-1;
            }  
        }
        return result;
        
    }
    public static int [][] idx2bitvect(int [][] indices, int M){
        int [][] v = new int [indices.length][M];
        for(int i=0;i<v.length;i++){
            for(int j=0;j<v[i].length;j++){
                v[i][j]=0;
            }
        }
        for(int i=0;i<indices.length;i++){
            for(int j=0;j<indices[i].length;j++){
                v[i][indices[i][j]]=1;
            }
        }
        return v;
    }
    public static int [][][] iterate_all_bv2(int m){
        int [][][] y = new int [m*m][m][m];
        int [][][] it = new int [m*m][0][0];
        for(int n=1;n<m+1;n++){
            it[n-1]=iterate(m,n);
        }
        for(int w=0;w<it.length;w++){
             y[w]=idx2bitvect(it[w], m);
        }
        int [][][] tempA = new int [m*m+1][1][m];
        for(int w=0;w<y.length;w++){
            tempA[w+1]=y[w];
        }
        y=tempA;
        for(int z=0;z<m;z++){
            for(int x=0;x<m;x++){
                y[0][0][x]=0;
            }
        }
        return y;
    }
    public static int [][] iterate_all_bv2small(int m){
        int mm = 1;
        for(int n=0;n<m;n++){
            mm= mm*2;
        }
        int [][][] y = new int [mm][m][m];
        int [][][] it = new int [mm][0][0];
        for(int n=1;n<m+1;n++){
            it[n-1]=iterate(m,n);
        }
        for(int w=0;w<it.length;w++){
             y[w]=idx2bitvect(it[w], m);
        }
        int [][][] tempA = new int [mm+1][1][m];
        for(int w=0;w<y.length;w++){
            tempA[w+1]=y[w];
        }
        y=tempA;
        for(int z=0;z<m;z++){
            for(int x=0;x<m;x++){
                y[0][0][x]=0;
            }
        }
        int [][] smally = new int [mm][m];
        int q=0;
        for(int w=0;w<y.length;w++){
            for(int j=0;j<y[w].length;j++){
                int r = 0;
                for(int k=0;k<y[w][j].length;k++){
                    smally[q][r]=y[w][j][k];
                    r++;
                }
                q++;
            }
        }
        //delete first row
        int [][] TsmallY = new int [mm-1][m];
        q=0;
        for(int i=1;i<smally.length;i++){
            TsmallY[q]=smally[i];
            q++;
        }
        smally=TsmallY;
        //end delete
        return smally;
    }
    public static void main(String[] args){
        int m=4;int n=2;
        int [][] y = iterate_all_bv2small(m);
        for(int w=0;w<y.length;w++){
            for(int j=0;j<y[w].length;j++){               
                System.out.print(y[w][j]);                
           }
            System.out.println();
        }
        System.out.println("done");
        System.out.println((3*3*2-1)+" "+y.length);

//        y = iterate(80,3);
//        for(int w=0;w<y.length;w++){
//            for(int j=0;j<y[w].length;j++){               
//                System.out.print(y[w][j]);                
//           }
//            System.out.println();
//        }
    }    
}

