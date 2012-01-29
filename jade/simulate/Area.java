package jade.simulate;
/*
 * this should house all the variables such as 
 * area specific extinction and connections
 */
import java.util.*;

public class Area {
	public Area(int index){
		this.index = index;
		con = new ArrayList<Connection>();
		exFunc = new ArrayList<ExtinctionFunction>();
		succdisps = new HashMap<Area,Integer> ();
	}
	
	public int getIndex(){return index;}
	public void addConnection(Connection in){
		con.add(in);
	}
	/*
	 * start should be older than end
	 */
	public void addConnection(Area dest,double start, double end, double func){
		Connection nc = new Connection(dest,start,end,func);
		con.add(nc);
	}
	public void addExtinction(double start, double end, double func){
		ExtinctionFunction xf = new ExtinctionFunction(start, end, func);
		exFunc.add(xf);
	}
	/*
	 * functional functions
	 */
	/**
     * 
     * @return a relavent connection given the time
     */
    public Connection getRandomRelaventConnection(double time){
    	ArrayList<Connection> tempcons = new ArrayList<Connection>();
    	for(int i=0;i<con.size();i++){
    		double st = con.get(i).getStart();
    		double et = con.get(i).getEnd();
    		if(time<=st&&time>=et){
    			tempcons.add(con.get(i));
    		}
    	}
    	if (tempcons.size()==0){
    		//System.out.println("arg");
    		return null;
    	}
    	else{
    		int x = new Random().nextInt(tempcons.size());
    		//System.out.println(tempcons.size());
    		return tempcons.get(x);
    	}
    }
	
    public double getExtinctionRate(double time){
    	double retextrate = extrate;
    	for(int i=0;i<exFunc.size();i++){
    		if(time <= exFunc.get(i).getStartTime()&&time > exFunc.get(i).getEndTime()){
    			retextrate = exFunc.get(i).getFunction();
    		}
    	}
    	return retextrate;
    }
    
    /*
     * for constrained, area specific extinction
     */
    public void setLocalExtinctionRate(double rate){
    	areaspecificextinction = rate;
    }
    public double getLocalExtinctionRate(){
    	return areaspecificextinction;
    }
    /*
     * for constrained, area specific dispersal
     * add from old to new
     */
    public void setPeriods(ArrayList<Double> periodtimes){
    	dispersalVectorStartTimes = periodtimes;
    	dispersalVector = new ArrayList<HashMap<Area,Double>>();
    	for(int i = 0; i < periodtimes.size(); i ++){
    		dispersalVector.add(new HashMap<Area, Double>());
    	}
    }
    public void setLocalDispersalRate(Area area, double rate, int period){
    	dispersalVector.get(period).put(area, rate);
    }
    public double getLocalDispersalRate(Area area, double time){
    	int per = 0;
    	if(dispersalVectorStartTimes.size()>1){
    		for(int i=0;i<dispersalVectorStartTimes.size();i++){
    			double st = dispersalVectorStartTimes.get(i);
    			double en = 0;
    			if((i+1)<dispersalVectorStartTimes.size()){
    				en = dispersalVectorStartTimes.get(i+1);
    			}
    			if(time <= st && time > en){
    				per = i;
    				break;
    			}else{
    				per++;
    			}
    		}
    	}
    	return dispersalVector.get(per).get(area);
    }
    public void addSuccDisp(Area area){
    	if(succdisps.containsKey(area)==false){
    		succdisps.put(area, 1);
    	}else{
    		succdisps.put(area, succdisps.get(area)+1);
    	}
    }
    
    public int getSuccDisp(Area area){
    	return succdisps.get(area);
    }
    
	/*
	 * private
	 */
    private HashMap<Area,Integer> succdisps; 
    private double areaspecificextinction = 1;
    private double extrate=1;
	private ArrayList<Connection>con;
	private ArrayList<ExtinctionFunction>exFunc;
	private int index;
	private ArrayList<HashMap<Area,Double>> dispersalVector;
	private ArrayList<Double> dispersalVectorStartTimes;
}
