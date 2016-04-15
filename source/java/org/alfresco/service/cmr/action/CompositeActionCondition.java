/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
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
