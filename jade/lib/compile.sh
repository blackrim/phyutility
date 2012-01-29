gcc -c -I/home/smitty/apps/jdk/jdk1.6.0/include -I/home/smitty/apps/jdk/jdk1.6.0/include/linux -o libmatrixExp.o  jade_reconstruct_area_PJNI.c 
ld -shared -o libmatrixExp.so libmatrixExp.o -lm -lgsl -lgslcblas

