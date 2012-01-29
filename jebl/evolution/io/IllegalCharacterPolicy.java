package jebl.evolution.io;

/**
 * What to do when an imported document contains illegal characters
 *
 * @author Tobias Thierer
 * @version $Id: IllegalCharacterPolicy.java 551 2006-12-04 03:05:01Z twobeers $
 *          <p/>
 *          created on 04.12.2006 15:15:54
 */
public enum IllegalCharacterPolicy {
    abort("Abort"),
    strip("Strip offending characters"),
    askUser("Ask");

    public final String description;
    IllegalCharacterPolicy(String description) {
        this.description = description;
    }

    public static IllegalCharacterPolicy instanceOf(String description) {
        for (IllegalCharacterPolicy p : values()) {
            if (p.description.equals(description)) {
                return p;
            }
        }
        return null;
    }

    public String toString() {
        return description;
    }
}
