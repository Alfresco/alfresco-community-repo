/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.service.cmr.action;

import java.io.Serializable;
import java.util.List;

/**
 * @author Nick Smith
 */
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
