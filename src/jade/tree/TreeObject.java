package jade.tree;

public class TreeObject {
	private String name;
	private Object obj;
	
	public TreeObject(String name, Object obj){
		this.name = name;
		this.obj = obj;
	}
	
	public Object getObject(){return obj;}
	public String getName(){return name;}
	public void setObject(Object obj){this.obj = obj;}
	public void setName(String name){ this.name = name;}
}
