package jebl.evolution.characters;

/**
 * @author Stephen A. Smith
 *
 */
public interface CharacterType {
	String getName();
	
	public static final CharacterType DISCRETE = new CharacterType() {
		public String getName(){ return "DISCRETE"; }
	};
	
	public static final CharacterType CONTINUOUS = new CharacterType() {
		public String getName(){ return "CONTINUOUS"; }
	};
}
