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
package org.alfresco.repo.action;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.CompositeActionCondition;

/**
 * Composite action condition implementation
 * 
 * @author Jean Barmash
 */

public class CompositeActionConditionImpl extends ActionConditionImpl implements CompositeActionCondition 
{

    public CompositeActionConditionImpl(String id) 
    {
        super(id, CompositeActionCondition.COMPOSITE_CONDITION);
    }
   
    private static final long serialVersionUID = -5987435419674390938L;
   
    /**
     * The actionCondition list
     */
    private List<ActionCondition> actionConditions = new ArrayList<ActionCondition>();
   
    private static Boolean OR = true;
    private static Boolean AND = false;
   
    private Boolean AndOr = AND;
   
    public void addActionCondition(ActionCondition actionCondition) 
    {
        this.actionConditions.add(actionCondition);
    }
   
    public void addActionCondition(int index, ActionCondition actionCondition) 
    {
        this.actionConditions.add(index, actionCondition);
    }
   
    public ActionCondition getActionCondition(int index) 
    {
        return this.actionConditions.get(index);
    }
   
    public List<ActionCondition> getActionConditions() 
    {
        return this.actionConditions;
    }
   
    public boolean hasActionConditions() 
    {
        return (this.actionConditions.isEmpty() == false);
    }
   
    public int indexOfActionCondition(ActionCondition actionCondition) 
    {
        return this.actionConditions.indexOf(actionCondition);
    }
   
    public void removeActionCondition(ActionCondition actionCondition) 
    {
        this.actionConditions.remove(actionCondition);
    }
   
    public void removeAllActionConditions() 
    {
        this.actionConditions.clear();
    }
   
    public void setActionCondition(int index, ActionCondition actionCondition) 
    {
        this.actionConditions.set(index, actionCondition);
    }
   
    public boolean isORCondition() 
    {
        return AndOr;
    }
   
    public void setORCondition(boolean andOr) 
    {
        AndOr = andOr;
    }
}
