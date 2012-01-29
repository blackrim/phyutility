package jade.reconstruct.area;

public class AncSplit {
	public AncSplit(int [] ancdists, int [][] descdists, double weight, double likelihood){
		this.weight = weight;
		this.likelihood = likelihood;
		this.ancdist = ancdists;
		this.descdists = descdists;
	}
	private double weight;
	private double likelihood;
	private int [][] descdists;
	private int [] ancdist;
	
	public double get_weight(){return weight;}
	public double get_like(){return likelihood;}
	public int [] get_ancdist(){return ancdist;}
	public int [][] get_descdists(){return descdists;}
	
}
