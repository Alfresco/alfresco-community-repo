/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service.cmr.action;

import java.util.List;

/**
 * Composite action condition
 * 
 * @author Jean Barmash
 */
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
