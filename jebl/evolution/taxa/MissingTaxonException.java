package jebl.evolution.taxa;

/**
 * @author Andrew Rambaut
 * @version $Id: MissingTaxonException.java 304 2006-04-25 11:04:53Z rambaut $
 */
public class MissingTaxonException extends Throwable {
	public MissingTaxonException(Taxon taxon) {
		super("Taxon, " + taxon.getName() + ", is missing.");
	}
}
