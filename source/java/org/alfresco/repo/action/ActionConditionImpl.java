/*
 * Copyright (C) 2005 Alfresco, Inc.
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

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.action.ActionCondition;

/**
 * @author Roy Wetherall
 */
public class ActionConditionImpl extends ParameterizedItemImpl implements Serializable,
        ActionCondition
{
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 3257288015402644020L;
    
    /**
     * Rule condition defintion
     */
    private String actionConditionDefinitionName;
    
    /**
     * Indicates whether the result of the condition should have the NOT logical operator applied 
     * to it.
     */
    private boolean invertCondition = false;

    /**
     * Constructor
     */
    public ActionConditionImpl(String id, String actionConditionDefinitionName)
    {
        this(id, actionConditionDefinitionName, null);
    }

    /**
     * @param parameterValues
     */
    public ActionConditionImpl(
    		String id,
            String actionConditionDefinitionName, 
            Map<String, Serializable> parameterValues)
    {
        super(id, parameterValues);
        this.actionConditionDefinitionName = actionConditionDefinitionName;
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionCondition#getActionConditionDefinitionName()
     */
    public String getActionConditionDefinitionName()
    {
        return this.actionConditionDefinitionName;
    }
    
    /**
     * @see org.alfresco.service.cmr.action.ActionCondition#setInvertCondition(boolean)
     */
    public void setInvertCondition(boolean invertCondition)
    {
        this.invertCondition = invertCondition;
    }
    
    /**
     * @see org.alfresco.service.cmr.action.ActionCondition#getInvertCondition()
     */
    public boolean getInvertCondition()
    {
        return this.invertCondition;
    }    
}
