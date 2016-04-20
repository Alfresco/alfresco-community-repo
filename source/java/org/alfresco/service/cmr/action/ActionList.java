
package org.alfresco.service.cmr.action;

import java.io.Serializable;
import java.util.List;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * @author Nick Smith
 */
@AlfrescoPublicApi
public interface ActionList<A extends Action> extends Serializable
{
    /**
     * Indicates whether there are any actions
     * 
     * @return true if there are actions, false otherwise
     */
    boolean hasActions();

    /**
     * Add an action to the end of the list
     * 
     * @param action the action
     */
    void addAction(A action);

    /**
     * Add an action to the list at the index specified
     * 
     * @param index the index
     * @param action the action
     */
    void addAction(int index, A action);

    /**
     * Replace the action at the specfied index with the passed action.
     * 
     * @param index the index
     * @param action the action
     */
    void setAction(int index, A action);

    /**
     * Gets the index of an action
     * 
     * @param action the action
     * @return the index
     */
    int indexOfAction(A action);

    /**
     * Get list containing the actions in their current order
     * 
     * @return the list of actions
     */
    List<A> getActions();

    /**
     * Get an action at a given index
     * 
     * @param index the index
     * @return the action
     */
    A getAction(int index);

    /**
     * Remove an action from the list
     * 
     * @param action the action
     */
    void removeAction(A action);

    /**
     * Remove all actions from the list
     */
    void removeAllActions();
}
