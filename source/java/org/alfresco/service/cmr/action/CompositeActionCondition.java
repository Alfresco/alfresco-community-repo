package org.alfresco.service.cmr.action;

import java.util.List;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Composite action condition
 * 
 * @author Jean Barmash
 */
@AlfrescoPublicApi
public interface CompositeActionCondition extends ActionCondition
{

    public static String COMPOSITE_CONDITION = "composite-condition";

    /**
     * Indicates whether there are any Conditions
     * 
     * @return  true if there are ActionConditions, false otherwise
     */
    boolean hasActionConditions();

    /**
     * Add an ActionCondition to the end of the list
     * 
     * @param ActionCondition  the ActionCondition
     */
    void addActionCondition(ActionCondition ActionCondition);

    /**
     * Add an ActionCondition to the list at the index specified
     * 
     * @param index        the index
     * @param ActionCondition    the ActionCondition
     */
    void addActionCondition(int index, ActionCondition ActionCondition);

    /**
     * Replace the ActionCondition at the specified index with the passed ActionCondition.
     * 
     * @param index        the index
     * @param ActionCondition    the ActionCondition
     */
    void setActionCondition(int index, ActionCondition ActionCondition);

    /**
     * Gets the index of an ActionCondition
     * 
     * @param ActionCondition    the ActionCondition
     * @return            the index
     */
    int indexOfActionCondition(ActionCondition ActionCondition);

    /**
     * Get list containing the ActionConditions in their current order
     * 
     * @return  the list of ActionConditions
     */
    List<ActionCondition> getActionConditions();

    /**
     * Get an ActionCondition at a given index
     * 
     * @param index        the index
     * @return            the ActionCondition
     */
    ActionCondition getActionCondition(int index);

    /**
     * Remove an ActionCondition from the list
     * 
     * @param ActionCondition  the ActionCondition
     */
    void removeActionCondition(ActionCondition ActionCondition);

    /**
     * Remove all ActionConditions from the list
     */
    void removeAllActionConditions();

    public boolean isORCondition();

    public void setORCondition(boolean andOr);    

}
