package jade.data;

public class Sequence {
	public Sequence (String id, String sequence){
		this.id=id;
		this.sequence=sequence;
	}
	public Sequence(){}
	public String getID(){return id;}
	public String getSeq(){return sequence;}
	public void setID(String id){this.id = id;}
	public void seqSeq(String seq){this.sequence = seq;}
	private String id;
	private String sequence;
}
