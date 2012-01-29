#include <jni.h>
#include "jade_reconstruct_area_PJNI.h"
#include <gsl/gsl_linalg.h>
#include <stdio.h>

JNIEXPORT jdoubleArray JNICALL Java_jade_reconstruct_area_PJNI_matrixExp
  (JNIEnv *jenv, jobject obj, jdoubleArray arr,jint size){
	jdoubleArray ret = (*jenv)->NewDoubleArray(jenv,(jint)( size * size)); 
	double *OutData = (*jenv)->GetDoubleArrayElements(jenv,ret,JNI_FALSE);
	//double OutData [size*size];
	jdouble * at_data = (*jenv)->GetDoubleArrayElements(jenv, arr, NULL);
	int i,j;
	double a_data [size*size];
	for (i = 0; i < size*size; i++){
		a_data[i] = (double)at_data[i];
	}
	gsl_matrix_view m = gsl_matrix_view_array (a_data, (int)size, (int)size);
	gsl_mode_t mt = 0;
	gsl_matrix *ma = gsl_matrix_alloc (size,size);
	gsl_linalg_exponential_ss(&m.matrix, ma, mt);//m input, ma output
	
	int x = 0;
	for(i = 0; i < size; i++){
		for(j=0;j<size;j++){
			OutData[x] = gsl_matrix_get(ma,i,j);
			x++;
		}	
	}
	//gsl_permutation_free (ma);
 	//(*jenv)->SetDoubleArrayRegion(jenv,ret,(jsize)0,(jsize)size*size,OutData);
	(*jenv)->ReleaseDoubleArrayElements(jenv,ret,OutData,0);
	(*jenv)->ReleaseDoubleArrayElements(jenv,arr,at_data,0);
	gsl_matrix_free (ma);
	return ret;
}
