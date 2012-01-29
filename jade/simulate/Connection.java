package jade.simulate;

public class Connection {
	public Connection(Area dest, double start, double end, double func){
		this.dest = dest;
		this.start = start;
		this.end = end;
		this.func = func;
	}
	public Area getDest(){return dest;}
	public double getStart(){return start;}
	public double getEnd(){return end;}
	public double getFunc(){return func;}
	private Area dest;
	private double start;
	private double end;
	private double func;
}
