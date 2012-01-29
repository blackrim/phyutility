package jade.simulate;

public class ExtinctionFunction {
	public ExtinctionFunction(double intime, double endtime, double value){
		start = intime;
		end = endtime;
		this.value = value;
	}
	public double getStartTime(){return start;}
	public double getEndTime(){return end;}
	public double getFunction(){
        return value;
    }
    public void setTimes(double intime, double endtime){
        start = intime;
        end = endtime;
    }
    public String toString(){
        String x = "";
        x = "from "+start+" to "+end+" connection is 0 else extinction is "+value;
        return x;
    }
    //start time
    private double start;
    //end time
    private double end;
    //value
    private double value;
}
