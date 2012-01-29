package jebl.evolution.align;

class TracebackAffine extends Traceback {

    int k;

    public TracebackAffine(int k, int i, int j) {

        this.k = k;
        this.i = i;
        this.j = j;
    }
    
    public final void setTraceback(int k, int i, int j) {
    	this.i = i;
    	this.j = j;
    	this.k = k;
    }
}
