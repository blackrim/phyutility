/**
 * RuleModel.java
 */

package org.virion.jam.panels;


/**
 * RuleModel.
 *
 * @author Andrew Rambaut
 * @version $Id: RuleModel.java 182 2006-01-23 21:24:01Z rambaut $
 */


public interface RuleModel {

    /**
     * Returns an array of strings to be presented as a combo box which
     * are available fields to define rules on.
     *
     * @return the field names
     */
    Object[] getFields();

    /**
     * Returns an array of strings to be presented as a combo box which
     * are possible rule conditions for the specified field.
     *
     * @return the condition names
     */
    Object[] getConditions(Object field);

    /**
     * Returns an array of strings to be presented as a combo box which
     * are possible values for the field. Should return null if a text
     * box is required.
     *
     * @return the values
     */
    Object[] getValues(Object field, Object condition);

}