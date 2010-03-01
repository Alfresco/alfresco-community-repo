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
