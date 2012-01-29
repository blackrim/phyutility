package jebl.evolution.characters;

import java.util.*;

import jebl.evolution.taxa.*;

/**
 * @author Stephen A. Smith
 *
 */
public class DiscreteCharacter implements Character{

	/**
	 * Constructs a basic DiscreteCharacter object with no taxa added yet
	 * @param name the name of the character
	 * @param desc the description of the character
	 * @param numOfStates the number of possible states for the character
	 */
	public DiscreteCharacter(String name, String desc, int numOfStates) {
		this.name = name;
		this.charType = CharacterType.DISCRETE;
		this.desc = desc;
		this.numOfStates = numOfStates;
		this.taxa = new HashSet <Taxon> ();
    }
	/**
	 * Constructs a basic DiscreteCharacter object with taxa
	 * @param name the name of the character
	 * @param desc the description of the character
	 * @param numOfStates the number of possible states for the character
	 * @param taxa the Set<Taxon> containing the taxa with this character
	 */
	public DiscreteCharacter(String name, String desc, int numOfStates, Set<Taxon> taxa) {
		this.name = name;
		this.charType = CharacterType.DISCRETE;
		this.desc = desc;
		this.numOfStates = numOfStates;
		this.taxa = taxa;
    }
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){ return name; }
	
	public void setDesc(String desc){
		this.desc = desc;
	}
	
	public String getDesc(){ return desc; }
	
	public CharacterType getType(){
		return charType;
	}
	
	public void addTaxon(Taxon taxon){
		taxa.add(taxon);
	}
	
	public Object getValue(Taxon taxon){
		int value = ((Integer)taxon.getAttribute(name)).intValue();
		return value;
	}
	
	/**
	 * @return whether character is ordered or not
	 */
	public boolean isOrdered(){ return isOrdered;}
	
	/**
	 * 
	 * @param isOrdered set whether character is ordered or not
	 */
	public void setIsOrdered(boolean isOrdered){
		this.isOrdered = isOrdered;
	}
	
	/**
	 * 
	 * @return the number of possible states for the character
	 */
	public double getNumOfStates(){ return numOfStates; }
	
	/**
	 * 
	 * @param numOfStates the number of possible states for the characeter
	 */
	public void setNumOfStates(int numOfStates){
		this.numOfStates = numOfStates;
	}
	
	public Set<Taxon> getTaxa(){ return taxa; }
	
	/**
	 * 
	 * @param stateDesc a Map<Integer, String> of the state descriptions corresponding to the values
	 */
	public void setStateDesc(Map <Integer, String> stateDesc){
		this.stateDesc = stateDesc;
	}
	
	/**
	 * 
	 * @return the Map<Integer, String> of the state descriptions corresponding to the values
	 */
	public Map <Integer, String> getStateDesc(){ return stateDesc; }
	
	/**
	 * 
	 * @param state corresponding to the state
	 * @return state description
	 */
	public String getStateDesc(int state){
		return stateDesc.get(state);
	}
	
	private boolean isOrdered;
	private String name;
	private String desc;
	private CharacterType charType;
	private Set<Taxon> taxa;
	private Map<Integer, String> stateDesc;
	private int numOfStates;
}
