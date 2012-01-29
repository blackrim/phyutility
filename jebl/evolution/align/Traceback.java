package jebl.evolution.align;

public abstract class Traceback {

    int i, j;                     // absolute coordinates
    
    public final int getX() { return i; }

    public final int getY() { return j; }

    public String toString() { return "("+getX() + ", " + getY()+")";};
}
