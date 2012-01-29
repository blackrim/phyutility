package jebl.evolution.characters;

import jebl.evolution.taxa.*;
import java.util.*;

/**
 * @author Stephen A. Smith
 *
 */
public interface Character{
	
	/**
	 * set the name of the character
	 * @param name the name of the character
	 */
	public void setName(String name);
	
	/**
	 * return the name of the character
	 * @return the name of the character
	 */
	public String getName();
	
	/**
	 * set the description of the character
	 * @param desc the description of the character
	 */
	public void setDesc(String desc);
	
	/**
	 * return the description of the character
	 * @return the description of the character
	 */
	public String getDesc();
	
	/**
	 * return the CharacterType of the character
	 * @return the CharacterType of the character 
	 */
	public CharacterType getType();
	
	/**
	 * add a taxon with this character
	 * @param taxon the taxon to add containing the character
	 */
	public void addTaxon(Taxon taxon);
	
	/**
	 * get a value for a taxon containing the character
	 * @param taxon the taxon to get the value for
	 * @return the Object value of the character for the given taxon
	 */
	public Object getValue(Taxon taxon);

	/**
	 * get a Set<Taxon> of all the taxa for this character
	 * @return a Set<Taxon> containing all of the taxa for this character
	 */
	public Set<Taxon> getTaxa();
}
