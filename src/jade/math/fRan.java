package jade.math;

import java.util.Random;

public class fRan {
	public static double fRan(double d){
		int RAN_IA	=	16807;
		int RAN_IM	=	2147483647;
		double RAN_AM	=	(1.0/RAN_IM);
		int RAN_IQ	=	127773;
		int RAN_IR	=	2836;
		int RAN_NTAB	=32;
		int RAN_NDIV	=(1+(RAN_IM-1)/RAN_NTAB);
		double RAN_EPS	=	1.2e-7;
		double RAN_RNMX	=(1.0-RAN_EPS);
		long seed = (new Random()).nextLong();
		
		double temp;
		int j;
		long k;
		long iy = 0;
		long [] iv = new long[RAN_NTAB];
		
		if (seed <= 0 || iy == 0) {
			if (-(seed) < 1)
				seed = 1;
			else
				seed = -(seed);
			for (j = RAN_NTAB+7; j >= 0; j--) {
				k = (seed)/RAN_IQ;
				seed = RAN_IA*(seed-k*RAN_IQ)-RAN_IR*k;
				if (seed < 0)
					seed += RAN_IM;
				if (j < RAN_NTAB)
					iv[j] = seed;
			}
			iy = iv[0];
		}
		k = (seed)/RAN_IQ;
		seed = RAN_IA*(seed-k*RAN_IQ)-RAN_IR*k;
		if (seed < 0)
			seed += RAN_IM;
		j = (int)iy/RAN_NDIV;//(int)
		iy = iv[j];
		iv[j] = seed;
		if ((temp=RAN_AM*iy) > RAN_RNMX)
			return d*RAN_RNMX;
		else
			return d*temp;
	}
	public static void main(String [] args){
		for(int i=0;i<1000;i++){
			System.out.println((fRan.fRan(0.1)));
		}
	}
	
}
